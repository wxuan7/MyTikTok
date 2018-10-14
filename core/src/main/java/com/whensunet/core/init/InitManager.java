package com.whensunet.core.init;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.whensunet.core.init.module.ImageManagerInitModule;
import com.whensunet.core.init.module.PreferenceInitModule;
import com.whensunset.annotation.singleton.Singleton;
import com.whensunset.logutil.debuglog.DebugLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by whensunset on 2018/10/3.
 */
@Singleton
public class InitManager {
  private static final String TAG = "INIT_MANAGER";
  private static final long MAX_DELAY_ACTIVITY_DISPLAY = 5000L;
  private static final long MIN_COST_FOR_REPORT = 10; // 10ms以内不记录
  private static final String METHOD_ON_APPLICATION_ATTACH_BASE_CONTEXT =
      "onApplicationAttachBaseContext";
  private static final String METHOD_ON_APPLICATION_CREATE = "onApplicationCreate";
  private static final String METHOD_ON_ACTIVITY_CREATE = "onMainActivityCreate";
  private static final String METHOD_ON_ACTIVITY_RESUME = "onMainActivityResume";
  private static final String METHOD_ON_ACTIVITY_DESTROY = "onMainActivityDestroy";
  private static final String METHOD_ON_BACKGROUND = "onCurrentActivityBackground";
  private static final String METHOD_ON_FOREGROUND = "onCurrentActivityForeground";
  private static final String METHOD_ON_ACTIVITY_LOAD_FINISHED_OR_AFTER_CREATE_10S =
      "onMainActivityLoadFinished";
  private final Set<InitModule> mTasks = new LinkedHashSet<>();
  private final Map<String, Map<String, Long>> mCosts = new HashMap<>();
  long MAX_DELAY_ACTIVITY_LOADED = 10000L;
  private boolean mCostReported;
  private boolean mActivityLoadFinished;
  
  public InitManager() {
    // Preference是全局使用的, 要先初始化
    mTasks.add(new PreferenceInitModule());
    mTasks.add(new ImageManagerInitModule());
    
    Iterator<InitModule> iterator = mTasks.iterator();
    while (iterator.hasNext()) {
      if (iterator.next() == null) {
        iterator.remove();
      }
    }
  }
  
  public void onApplicationAttachBaseContext(Context base) {
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onApplicationAttachBaseContext(base);
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_APPLICATION_ATTACH_BASE_CONTEXT, end - start);
    }
  }
  
  public void onApplicationCreate(Application application) {
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onApplicationCreate(application);
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_APPLICATION_CREATE, end - start);
    }
  }
  
  public void onMainActivityCreate(Activity activity, Bundle savedInstanceState) {
    mCostReported = false;
    mActivityLoadFinished = false;
    
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onMainActivityCreate(activity, savedInstanceState);
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_ACTIVITY_CREATE, end - start);
    }
    final Handler handler = new Handler();
    
    // 兜底ActivityLoadFinishEvent
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
      }
    }, MAX_DELAY_ACTIVITY_LOADED);
  }
  
  public void onMainActivityResume(Activity activity) {
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onMainActivityResume(activity);
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_ACTIVITY_RESUME, end - start);
    }
  }
  
  public void onMainActivityDestroy(Activity activity) {
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onMainActivityDestroy(activity);
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_ACTIVITY_DESTROY, end - start);
    }
  }
  
  // todo 需要用 类似 eventbus 来在主 Activity 真正加载完成的时候例如 首页的数据都显示出来了 通知这个方法进行调用，可能是 rxbus
  public void onMainActivityLoadFinished() {
    if (mActivityLoadFinished) {
      return;
    }
    mActivityLoadFinished = true;
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onMainActivityLoadFinished();
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_ACTIVITY_LOAD_FINISHED_OR_AFTER_CREATE_10S, end - start);
    }
    // 使用主 Activity 加载数据完毕来当做结束计时的时间点
    reportCost();
  }
  
  // todo 需要用 类似 eventbus 来在当前 Activity stop 的时候 通知这个方法进行调用，可能是 rxbus
  public void onCurrentActivityBackground() {
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onCurrentActivityBackground();
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_BACKGROUND, end - start);
    }
  }
  
  // todo 需要用 类似 eventbus 来在当前 Activity start的时候 通知这个方法进行调用，可能是 rxbus
  public void onCurrentActivityForeground() {
    for (InitModule task : mTasks) {
      long start = SystemClock.elapsedRealtime();
      try {
        task.onCurrentActivityForeground();
      } catch (Throwable ignored) {
        DebugLogger.e(TAG, "error", ignored);
      }
      long end = SystemClock.elapsedRealtime();
      logCost(task, METHOD_ON_FOREGROUND, end - start);
    }
  }
  
  private void logCost(InitModule task, String method, long cost) {
    if (mCostReported) {
      return;
    }
    if (cost < MIN_COST_FOR_REPORT) {
      return;
    }
    Map<String, Long> map = mCosts.get(method);
    if (map == null) {
      map = new HashMap<>();
      mCosts.put(method, map);
    }
    map.put(task.getClass().getSimpleName(), cost);
  }
  
  private void reportCost() {
    if (mCostReported) {
      return;
    }
    for (Map.Entry<String, Map<String, Long>> entry : mCosts.entrySet()) {
      String method = entry.getKey();
      Map<String, Long> map = entry.getValue();
      
      long cost = 0;
      for (Map.Entry<String, Long> longEntry : map.entrySet()) {
        cost += longEntry.getValue();
      }
      
      reportMethodCost(method, cost, map);
    }
    mCosts.clear();
    mCostReported = true;
  }
  
  private void reportMethodCost(String method, long cost, Map<String, Long> map) {
    // todo 一个上报点
  }
}
