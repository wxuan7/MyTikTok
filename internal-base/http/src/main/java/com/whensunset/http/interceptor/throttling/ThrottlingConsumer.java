package com.whensunset.http.interceptor.throttling;

import com.whensunset.http.model.DataContainer;

import java.net.URL;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class ThrottlingConsumer implements Consumer<Object> {
  
  @Override
  public void accept(@NonNull Object o) throws Exception {
    if (!(o instanceof DataContainer)) {
      return;
    }
    DataContainer<?> dataContainer = (DataContainer<?>) o;
    URL url = null;
    if (dataContainer.raw() != null) {
      url = dataContainer.raw().request().url().url();
    }
    if (url == null) {
      return;
    }
    ThrottlingConfigHolder.getDefault().put(url.getPath(),
        new ThrottlingConfig(dataContainer.policyExpireMs(), dataContainer.nextRequestSleepMs()));
  }
  
}
