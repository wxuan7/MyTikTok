package com.whensunset.logutil.locallog;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;


/**
 * 用于记录调试日志.
 */
public class LocalFileLogger {
  
  private static final String ERROR_URL = "diagnosis_error";
  private static final String WARING_URL = "waring";
  private static final String INFO_URL = "info";
  private static final String DEBUG = "debug";
  private static LocalFileLogBinder sLogInterface;
  private static StringBuilderHolder sBuilderHolder = new StringBuilderHolder();
  
  // 是否有任意一项调试日志被打开
  public static boolean isEnableAnyDebugLog() {
    return true;
  }
  
  // 是否打开了Event类型的调试日志
  public static boolean isEnableDebugLogOfEvent() {
    return true;
  }
  
  
  public static void onEvent(Context context, String url, String actionName, Object... arguments) {
    if (!isEnableDebugLogOfEvent()) {
      return;
    }
    ensureInit(context);
    addLog(context, DEBUG, url + " " + actionName, null, arguments);
  }
  
  
  public static void onErrorEvent(Context context, String actionName, Throwable e, Object... arguments) {
    // error 级别的日志全部存储
    ensureInit(context);
    addLog(context, ERROR_URL, actionName, e, arguments);
  }
  
  public static void onWaring(Context context, String actionName, Object... arguments) {
    ensureInit(context);
    addLog(context, WARING_URL, actionName, null, arguments);
  }
  
  public static void onInfo(Context context, String actionName, Object... arguments) {
    ensureInit(context);
    addLog(context, INFO_URL, actionName, null, arguments);
  }
  
  public static void onEvent(Context context, String message) {
    onEvent(context, "", message);
  }
  
  private static void addLog(Context context, String url, String message, Throwable e, Object... arguments) {
    
    StringBuilder builder = sBuilderHolder.get();
    String content = "";
    if (sLogInterface != null) {
      if (arguments != null && arguments.length > 0) {
        for (Object obj : arguments) {
          if (obj != null) {
            if (builder.length() > 0) {
              builder.append(",");
            }
            builder.append(obj.toString());
          }
        }
      }
      
      if (e != null) {
        if (builder.length() > 0) {
          builder.append(",");
        }
        builder.append(e.getMessage());
      }
      content = builder.substring(0);
      try {
        sLogInterface.log(url, message, content);
      } catch (Throwable ignore) {
        retryAddLog(context, url, message, content);
      }
    } else {
      retryAddLog(context, url, message, content);
    }
  }
  
  private static void retryAddLog(Context context, String tag, String message, String args) {
    try {
      // 重新绑定 service.
      sLogInterface = null;
      ensureInit(context);
      
      // 如果调用 Binder 异常，通过 startService 的方式传递 log.
      Intent startIntent = new Intent(context, LocalFileLogService.class);
      startIntent.putExtra(LocalFileLogService.KEY_LOG, message);
      startIntent.putExtra(LocalFileLogService.KEY_TAG, tag);
      startIntent.putExtra(LocalFileLogService.KEY_CONTEXT, args);
      context.startService(startIntent);
    } catch (Exception e) {
    }
  }
  
  private static void ensureInit(Context context) {
    try {
      if (sLogInterface == null) {
        Intent intent = new Intent(context, LocalFileLogService.class);
        context.bindService(intent, new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            sLogInterface = LocalFileLogBinder.Stub.asInterface(service);
          }
          
          @Override
          public void onServiceDisconnected(ComponentName name) {
          }
        }, Service.BIND_AUTO_CREATE);
      }
    } catch (Exception ignored) {
    }
  }
  
}
