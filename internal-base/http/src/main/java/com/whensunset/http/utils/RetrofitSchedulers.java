package com.whensunset.http.utils;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RetrofitSchedulers {
  
  /**
   * UI线程.
   */
  public static final Scheduler MAIN = AndroidSchedulers.mainThread();
  
  /**
   * 用于执行网络请求.
   */
  public static final Scheduler NETWORKING = Schedulers.from(
      newFixedThreadPoolExecutor(4));
  
  /**
   * todo 后续需要定制 线程池
   * 用于执行异步任务, 例如读写缓存, 读写pref, 额外的API请求等.
   */
  public static final Scheduler ASYNC = Schedulers.from(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
      TimeUnit.SECONDS, new SynchronousQueue<>()));
  
  // todo 后续需要定制 线程池
  public static ThreadPoolExecutor newFixedThreadPoolExecutor(int size) {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(size, size, 1, TimeUnit.MINUTES,
        new LinkedBlockingQueue<>());
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }
  
}
