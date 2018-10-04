package com.whensunset.http.retrofit.interceptor.throttling;

import com.whensunset.http.retrofit.model.Response;

import java.net.URL;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class ThrottlingConsumer implements Consumer<Object> {

  @Override
  public void accept(@NonNull Object o) throws Exception {
    if (!(o instanceof Response)) {
      return;
    }
    Response<?> response = (Response<?>) o;
    URL url = null;
    if (response.raw() != null) {
      url = response.raw().request().url().url();
    }
    if (url == null) {
      return;
    }
    ThrottlingConfigHolder.getDefault().put(url.getPath(),
        new ThrottlingConfig(response.policyExpireMs(), response.nextRequestSleepMs()));
  }

}
