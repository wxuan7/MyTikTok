package com.whensunset.logutil.uploadlog.sender;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.whensunset.logutil.uploadlog.config.UploadLogConfiguration;
import com.whensunset.logutil.uploadlog.policy.UploadLogPolicy;
import com.whensunset.logutil.uploadlog.storage.UploadLogStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 处理 Log 发送和重传的机制
 */
public class UploadLogHandlerImpl implements UploadLogHandler {
  
  private static final String TAG = "@UploadLogHandlerImpl";
  private static final int INIT_DELAY_DURATION = 10 * 1000;
  private static final int MAX_RETRY_COUNT = 3;
  private static final long DEFAULT_RETRY_DURATION = 4 * 1000;
  private static final long MAX_RETENTION_TIME = 30L * 24 * 60 * 60 * 1000;
  private static final boolean DEBUG = false;
  
  private static final String KEY_MAX_LAST_SUCCESS_LOG_ID = "lastMaxSuccessLogId";
  
  private static final int MAX_LOG_NUM = 500; // 最多一次发送 500 条日志
  private final long mLogRetentionTime;
  private final UploadLogStorage mStorage;
  private final LogSender mLogSender;
  private final Context mContext;
  private final int mMaxFailedCount;
  
  private volatile long mLastSuccessLogId;
  private Handler mLogSenderHandler;
  private Handler mDelayedLogSenderHandler;
  private UploadLogConfiguration mLogConfiguration;
  private UploadLogPolicy.Upload mUploadPolicy = UploadLogPolicy.Upload.NORMAL;
  private HandlerThread mDelayedLogThread;
  private volatile long mUploadIntervalMs = TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES);
  
  public UploadLogHandlerImpl(Context context, UploadLogStorage storage, LogSender logSender,
                              UploadLogConfiguration logConfiguration) {
    mStorage = storage;
    mLogSender = logSender;
    mContext = context;
    mMaxFailedCount = logConfiguration.getMaxFailedCount();
    mLogConfiguration = logConfiguration;
    
    HandlerThread thread = new HandlerThread("log-sender", Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    mLogSenderHandler = new Handler(thread.getLooper());
    mLogRetentionTime = logConfiguration.getLogRetentionTime();
    mLogSenderHandler.postDelayed(() -> {
      sendLog();
      if (isNetworkConnected(mContext)) {
        clearLog();
      }
    }, INIT_DELAY_DURATION);
    
    mDelayedLogThread = new HandlerThread("delayed-log-sender", Process.THREAD_PRIORITY_BACKGROUND);
    mDelayedLogThread.start();
    mDelayedLogSenderHandler = new Handler(mDelayedLogThread.getLooper());
  }
  
  private long getUploadInterval() {
    if (mLogConfiguration.useDebugSendingInterval()) {
      return TimeUnit.MILLISECONDS.convert(3, TimeUnit.SECONDS);
    }
    return mUploadIntervalMs;
  }
  
  private void sendLog() {
    // 每间隔 getUploadInterval 的时间就发送一个 sendlog 的请求
    mLogSenderHandler.postDelayed(this::sendLog, getUploadInterval());
    
    // 如果当前没有网络链接，则不发送.
    if (isNetworkConnected(mContext)) {
      return;
    }
    Map<String, String> params = new HashMap<>();
    // 1 stands for normal logs
    params.put("priorityType", "1");
    // 本次请求发送的日志数量是  MAX_LOG_NUM
    sendBatchReportEvent(mLogSenderHandler, mStorage.getLogs(MAX_LOG_NUM), params);
  }
  
  /**
   * 发送实时日志，来一条发一条
   */
  public void sendRealLog(final Object[] logs) {
    if (logs != null) {
      mLogSenderHandler.post(() -> {
        Map<String, String> params = new HashMap<>();
        params.put("priorityType", "1");
        sendBatchReportEvent(mLogSenderHandler, logs, params);
      });
    }
  }
  
  // 优先级比较低的发送日志
  private void sendDelayedLog() {
    mDelayedLogSenderHandler.postDelayed(this::sendDelayedLog, getUploadInterval());
    
    if (!isNetworkConnected(mContext)) {
      return;
    }
    Map<String, String> params = new HashMap<>();
    // 2 stands for delayed logs
    params.put("priorityType", "2");
    sendBatchReportEvent(mDelayedLogSenderHandler, mStorage.getDelayedLogs(MAX_LOG_NUM), params);
  }
  
  /**
   * 批量发送日志，每次最多返送 {@link #MAX_LOG_NUM } 条日志，防止日志信息过大，导致发送失败.
   */
  private void sendBatchReportEvent(final Handler handler, Object[] logs,
                                    final Map<String, String> params) {
    if (mUploadPolicy == UploadLogPolicy.Upload.NONE) {
      return;
    }
    
    Object[] reports = logs;
    if (reports != null && reports.length > 0) {
      boolean result;
      result = mLogSender.send(logs, params);
      if (result) {
        mStorage.deleteLog(logs);
        long logId = 0;// todo logId 被储存在日志对象中
        synchronized (this) {
          if (logId > mLastSuccessLogId) {
            mLastSuccessLogId = logId;
            // todo 在未来 完善了 preferences 模块之后 记录一下当前的 mLastSuccessLogId
          }
        }
      } else {
        handler.postDelayed(() -> innerSendLog(handler, logs, 1, params), DEFAULT_RETRY_DURATION);
      }
    }
  }
  
  private void innerSendLog(final Handler handler,
                            final Object[] logs,
                            final int retryCount, final Map<String, String> params) {
    if (retryCount >= MAX_RETRY_COUNT) {
      for (Object reportEvent : logs) {
        // todo 未来的日志结构是由proto协议定义的 如果 proto结构发生变化，就会导致的数据为 null.
        if (reportEvent == null || mStorage == null) {
          // 如果发生数据错乱的话，直接清空所有的数据。
          if (mStorage != null) {
            mStorage.deleteAll();
          }
          
          return;
        }
        // 失败的时间或者次数超过配置的最大值，则丢弃该条 Log
        long logId = 0;// todo logId 被储存在日志对象中
        long retentionTime = mStorage.getFailedTime(logId);
        long failedCount = mStorage.getFailedCount(logId);
        if (retentionTime > mLogRetentionTime
            && failedCount >= mMaxFailedCount) {
          mStorage.deleteLog(logId);
        } else {
          // 记录当前 Log 的失败原因.
          mStorage.updateFailureLog(logId);
        }
      }
      return;
    }
    
    boolean result = mLogSender.send(logs, params);
    if (result) {
      if (logs != null) {
        mStorage.deleteLog(logs);
        long logId = 0;// todo logId 被储存在日志对象中
        synchronized (this) {
          if (logId > mLastSuccessLogId) {
            mLastSuccessLogId = logId;
            // todo 在未来 完善了 preferences 模块之后 记录一下当前的 mLastSuccessLogId
          }
        }
      }
    } else {
      handler.postDelayed(() -> innerSendLog(handler, logs, retryCount + 1, params), (int) (Math.pow(2, retryCount) * DEFAULT_RETRY_DURATION));
    }
  }
  
  @Override
  public void setUploadPolicy(UploadLogPolicy.Upload policy) {
    if (mUploadPolicy == policy) {
      return;
    }
    mUploadPolicy = policy;
    if (policy == UploadLogPolicy.Upload.ALL) {
      mDelayedLogSenderHandler.postDelayed(this::sendDelayedLog, INIT_DELAY_DURATION);
    } else if (policy == UploadLogPolicy.Upload.NORMAL) {
      mDelayedLogSenderHandler.removeCallbacksAndMessages(null);
    } else if (policy == UploadLogPolicy.Upload.NONE) {
      mLogSenderHandler.removeCallbacksAndMessages(null);
      mDelayedLogSenderHandler.removeCallbacksAndMessages(null);
    }
  }
  
  
  private void clearLog() {
    if (mLastSuccessLogId == 0) {
      return;
    }
    Object[] logs = mStorage.getLogBelowId(mLastSuccessLogId);
    if (logs != null && logs.length > 0) {
      for (Object reportEvent : logs) {
        // 如果 Log 生成的时间超过 30 天则删除 Log. todo 生成日志的时间会被记录在 reportEvent中
        long logGenerateTime = System.currentTimeMillis();
        if (logGenerateTime > MAX_RETENTION_TIME) {
          // 如果当前的 日志 存在 1个月前的日志，则将以前的 Log 全部丢弃.
          mStorage.deleteAll();
          synchronized (this) {
            mLastSuccessLogId = 0;
            // todo 在未来 完善了 preferences 模块之后 记录一下当前的 mLastSuccessLogId
          }
        }
      }
    }
  }
  
  public void setUploadIntervalMs(long intervalMs) {
    mUploadIntervalMs = Math.max(3000, intervalMs);
  }
  
  // todo 未来需要完善一个 NetWorkUtil 这个方法放进入
  private static boolean isNetworkConnected(Context context) {
    try {
      ConnectivityManager cm =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (cm == null) {
        return false;
      }
      NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
      return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    } catch (Exception e) {
      return false;
    }
  }
}
