package com.whensunset.http.consumer;

import com.whensunset.http.model.DataContainer;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class ResponseFunction<T> implements Function<DataContainer<T>, T> {
  
  @Override
  public T apply(@NonNull DataContainer<T> dataContainer) throws Exception {
    return dataContainer.body();
  }
}
