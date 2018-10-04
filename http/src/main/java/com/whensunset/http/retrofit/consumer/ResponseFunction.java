package com.whensunset.http.retrofit.consumer;

import com.whensunset.http.retrofit.model.Response;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class ResponseFunction<T> implements Function<Response<T>, T> {

  @Override
  public T apply(@NonNull Response<T> response) throws Exception {
    return response.body();
  }
}
