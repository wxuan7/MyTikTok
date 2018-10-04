package com.whensunet.core.init;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.whensunet.core.MytiktokApp;
import com.whensunet.core.config.SpeedConfig;
import com.whensunset.utils.SystemUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class InitModule {

  private static final long DELAY_DURATION = 5_000L;
  private static final long NORMAL_DELAY_DURATION = 3_000L;
  private static final long LONG_DELAY_DURATION = 10_000L;
  private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

  // todo 之后需要写一个模块 定制线程池 控制线程的创建
  private static final ThreadPoolExecutor BACKGROUND_THREAD_POOL =
      new ThreadPoolExecutor(0, 1, 10, TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(),
          (ThreadFactory) Thread::new);

  /**
   * NOTE: 这里只能做本地操作，不得发网络请求！！！
   * @param runnable
   */
  public void runOnBackgroundThreadImmediately(Runnable runnable) {
    BACKGROUND_THREAD_POOL.submit(runnable);
  }

  public void runOnBackgroundThread(Runnable runnable) {
    if (SpeedConfig.isSpeedLaunch()) {
      // 默认所有的任务都在启动后 3s 再进行
      UI_HANDLER.postDelayed(() -> BACKGROUND_THREAD_POOL.submit(runnable), NORMAL_DELAY_DURATION);
    } else {
      runOnBackgroundThreadImmediately(runnable);
    }
  }

  public void runOnBackgroundThreadDelay(final Runnable runnable) {
    UI_HANDLER.postDelayed(() -> {
      BACKGROUND_THREAD_POOL.submit(() -> runnable.run());
    }, DELAY_DURATION);
  }

  public void runOnBackgroundThreadLongDelay(final Runnable runnable) {
    UI_HANDLER.postDelayed(() -> BACKGROUND_THREAD_POOL.submit(() -> runnable.run()), LONG_DELAY_DURATION);
  }

  public void runOnceOnIdleHandler(final Runnable runnable) {
    Looper.myQueue().addIdleHandler(() -> {
      try {
        runnable.run();
      } catch (Throwable ignored) {}
      return false;
    });
  }

  protected boolean isInMainProcess() {
    // 只有主进程或者没有取到进程名称的时候，才下载资源.
    return SystemUtil.isInMainProcess(MytiktokApp.getAppContext())
        || TextUtils.isEmpty(SystemUtil.getProcessName(MytiktokApp.getAppContext()));
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getClass() == obj.getClass();
  }

  public void onApplicationAttachBaseContext(Context base) {}

  public void onApplicationCreate(Application application) {}

  public void onMainActivityCreate(Activity activity, Bundle savedInstanceState) {}

  public void onMainActivityResume(Activity activity) {}

  public void onMainActivityDestroy(Activity activity) {}

  public void onMainActivityLoadFinished() {}

  public void onCurrentActivityBackground() {}

  public void onCurrentActivityForeground() {}

}
