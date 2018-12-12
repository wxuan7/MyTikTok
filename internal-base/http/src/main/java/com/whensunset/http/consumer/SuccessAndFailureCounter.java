package com.whensunset.http.consumer;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class SuccessAndFailureCounter {
  
  public static final AtomicInteger SUCCESS_COUNT = new AtomicInteger(0);
  public static final AtomicInteger FAILURE_COUNT = new AtomicInteger(0);
  public static final Action ON_COMPLETE = SUCCESS_COUNT::incrementAndGet;
  public static final Consumer<Throwable> ON_ERROR = throwable -> FAILURE_COUNT.incrementAndGet();
  private SuccessAndFailureCounter() {
  }
  
  public void reset() {
    SUCCESS_COUNT.set(0);
    FAILURE_COUNT.set(0);
  }
}
