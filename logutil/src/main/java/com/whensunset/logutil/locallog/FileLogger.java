package com.whensunset.logutil.locallog;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileLogger {
  public static File ROOT_DIR = new File("/mnt/sdcard/mytiktok");
  private static final int MSG_ADD = 1;
  private static final int MSG_FLUSH = 2;
  private static final int LOG_CACHE_COUNT = 40; // 最多攒40条log后flush
  private static final long LOG_MAX_LENGTH = 40 * 1024 * 1024; // 文件最大40M
  private static final long TRIGGER_DELAY_DURATION = 40 * 1000; // 40s循环flush
  
  private static final String LOG_FILE_NAME = "debug.log";
  private static final String LOG_DIR = new File(ROOT_DIR, ".debug").getAbsolutePath();
  
  final List<String> mLogs;
  private final LoggerHandler mLoggerHandler;
  
  public FileLogger() {
    HandlerThread thread = new HandlerThread("FileLogger", Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    mLoggerHandler = new LoggerHandler(thread.getLooper());
    mLogs = new ArrayList<>();
    mLoggerHandler.sendEmptyMessage(MSG_FLUSH);
  }
  
  public void addLog(String log) {
    if (TextUtils.isEmpty(log)) {
      return;
    }
    Message message = Message.obtain(mLoggerHandler, MSG_ADD);
    message.obj = log;
    message.sendToTarget();
  }
  
  public static void sendLog() {
    // todo 未来实现，显示一个界面用于让用户可以将本地的日志文件上传服务器
  }
  
  public static List<String> getLogList(int maxNum, String... filters) {
    List<String> logList = new LinkedList<>();
    File logFile = getLogFile();
    if (logFile == null) {
      return logList;
    }
    FileInputStream is = null;
    InputStreamReader ir = null;
    BufferedReader br = null;
    try {
      is = new FileInputStream(logFile);
      ir = new InputStreamReader(is);
      br = new BufferedReader(ir);
      String line;
      do {
        line = br.readLine();
        if (!TextUtils.isEmpty(line)) {
          if (filters == null || filters.length == 0) {
            logList.add(0, line);
          } else {
            for (String filter : filters) {
              if (line.contains(filter)) {
                logList.add(0, line);
                break;
              }
            }
          }
        }
      } while (line != null);
      if (logList.size() > maxNum) {
        return logList.subList(0, maxNum);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      closeQuietly(br);
      closeQuietly(ir);
      closeQuietly(is);
    }
    return logList;
  }
  
  private static File getLogFile() {
    File logDir = new File(LOG_DIR);
    File logFile = new File(logDir, LOG_FILE_NAME);
    logDir.mkdirs();
    if (!logFile.exists()) {
      try {
        logFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (!logFile.isFile()) {
      // todo 文件创建失败
    }
    return logFile;
  }
  
  class LoggerHandler extends Handler {
    
    public LoggerHandler(Looper looper) {
      super(looper);
    }
    
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_ADD:
          add((String) msg.obj);
          break;
        case MSG_FLUSH:
          flush();
          break;
      }
    }
    
    void add(String log) {
      if (TextUtils.isEmpty(log)) {
        return;
      }
      mLogs.add(log);
      if (mLogs.size() >= LOG_CACHE_COUNT) {
        sendEmptyMessage(MSG_FLUSH);
      }
    }
    
    void flush() {
      if (mLogs.isEmpty()) {
        return;
      }
      List<String> copy = new ArrayList<>();
      copy.addAll(mLogs);
      mLogs.clear();
      writeLog(copy);
      // 定时触发下一次
      sendEmptyMessageDelayed(MSG_FLUSH, TRIGGER_DELAY_DURATION);
    }
    
    void writeLog(List<String> logs) {
      File logFile = getLogFile();
      if (logFile == null) {
        // 文件创建失败, 不写了
        return;
      }
      FileOutputStream os = null;
      OutputStreamWriter ow = null;
      FileChannel fc = null;
      try {
        os = new FileOutputStream(logFile, true);
        ow = new OutputStreamWriter(os, "utf-8");
        fc = os.getChannel();
        fc.lock();
        for (String log : logs) {
          ow.write(log);
        }
        ow.flush();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        closeQuietly(os);
        closeQuietly(ow);
        closeQuietly(fc);
      }
      if (logFile.length() > LOG_MAX_LENGTH) {
        trimToSize(logFile, LOG_MAX_LENGTH / 2);
      }
    }
    
    // 裁剪文件, 去掉前面的部分, 使其到达目标长度
    void trimToSize(File logFile, long length) {
      // 计算要跳过多少字节
      long skip = logFile.length() - length;
      if (skip <= 0) {
        return;
      }
      // 先把log拷贝一份
      File tempFile = new File(logFile.getPath() + ".temp");
      copyFile(logFile, tempFile);
      // 把tempFile向logFile复制
      FileInputStream in = null;
      FileOutputStream os = null;
      FileChannel fc = null;
      try {
        in = new FileInputStream(tempFile);
        os = new FileOutputStream(logFile);
        fc = os.getChannel();
        fc.lock();
        if (in.skip(skip) != skip) {
          // skip的长度不对, 好奇怪, 直接不裁剪了吧
          return;
        }
        // 向后找到第一个换行
        while (true) {
          int next = in.read();
          if (next == -1 || (char) next == '\n') {
            // 读到没有了或者遇到换行为止
            break;
          }
        }
        // 开始读取后面需要保留的内容
        byte[] buf = new byte[1024 * 8];
        int len;
        while ((len = in.read(buf)) != -1) {
          os.write(buf, 0, len);
        }
        os.flush();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        closeQuietly(in);
        closeQuietly(os);
        closeQuietly(fc);
        tempFile.delete();
      }
    }
  }
  
  // todo 未来需要完善一个 IOUtil 下面这些方法放进入
  public static void closeQuietly(OutputStream output) {
    closeQuietly((Closeable) output);
  }
  
  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }
  
  public static boolean copyFile(File srcFile, File dstFile) {
    if (srcFile.exists() && srcFile.isFile()) {
      if (dstFile.isDirectory()) {
        return false;
      }
      if (dstFile.exists()) {
        dstFile.delete();
      }
      try {
        byte[] buffer = new byte[2048];
        BufferedInputStream input = new BufferedInputStream(
            new FileInputStream(srcFile));
        BufferedOutputStream output = new BufferedOutputStream(
            new FileOutputStream(dstFile));
        while (true) {
          int count = input.read(buffer);
          if (count == -1) {
            break;
          }
          output.write(buffer, 0, count);
        }
        input.close();
        output.flush();
        output.close();
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }
}
