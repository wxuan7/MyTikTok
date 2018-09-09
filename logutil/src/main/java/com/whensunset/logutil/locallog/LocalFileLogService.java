package com.whensunset.logutil.locallog;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalFileLogService extends Service {

  private FileLogger mFileLogger = new FileLogger();
  public static final String KEY_LOG = "log";
  public static final String KEY_TAG = "tag";
  public static final String KEY_CONTEXT = "context";

  private StringBuilderHolder mBuilderHolder = new StringBuilderHolder(512);

  private Handler mWorkHandler;

  private SimpleDateFormat mTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

  private LocalFileLogBinder.Stub mBinder = new LocalFileLogBinder.Stub() {
    @Override
    public void log(String tag, String message, String context) throws RemoteException {
      mWorkHandler.post(() -> {
        String log = buildContent(tag, message, context);
        Log.d(KEY_LOG, log);
        mFileLogger.addLog(log);
      });
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();

    HandlerThread handlerThread = new HandlerThread("LocalFileLogService",
        Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();
    mWorkHandler = new Handler(handlerThread.getLooper());
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      return START_STICKY;
    }

    mWorkHandler.post(() -> {
      String message = intent.getStringExtra(KEY_LOG);
      String tag = intent.getStringExtra(KEY_TAG);
      String context = intent.getStringExtra(KEY_CONTEXT);
      if (!TextUtils.isEmpty(message)) {
        String log = buildContent(tag, message, context);
        Log.e(KEY_LOG, log);
        mFileLogger.addLog(log);
      }
    });

    return START_STICKY;
  }

  private String buildContent(String tag, String message, String context) {
    StringBuilder builder = mBuilderHolder.get();
    builder.append(mTimeFormat.format(new Date()) + "  ");
    if (!TextUtils.isEmpty(tag)) {
      builder.append(tag + "   ");
    }

    if (!TextUtils.isEmpty(message)) {
      builder.append(message + "   ");
    }

    if (!TextUtils.isEmpty(context)) {
      builder.append(context);
    }
    builder.append("\n");

    return builder.substring(0);
  }
}
