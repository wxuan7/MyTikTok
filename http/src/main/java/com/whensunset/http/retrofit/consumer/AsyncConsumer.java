package com.whensunset.http.retrofit.consumer;

import com.whensunset.http.retrofit.utils.RetrofitSchedulers;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class AsyncConsumer<T> implements Consumer<T> {

  private final Consumer<T> mActual;

  private AsyncConsumer(Consumer<T> actual) {
    mActual = actual;
  }

  @Override
  public void accept(@NonNull final T t) throws Exception {
    RetrofitSchedulers.ASYNC.scheduleDirect(() -> {
      try {
        mActual.accept(t);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  public static <T> AsyncConsumer<T> create(Consumer<T> actual) {
    return new AsyncConsumer<>(actual);
  }
}
