package com.whensunset.http.retrofit.interceptor.throttling;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ThrottlingInterceptor implements Interceptor {

  @Override
  public Response intercept(Chain chain) throws IOException {
    String path = chain.request().url().url().getPath();
    ThrottlingConfig config = ThrottlingConfigHolder.getDefault().remove(path);
    // 存在config且没有过期
    if (config != null && config.mPolicyExpireMs > System.currentTimeMillis()
        && config.mNextRequestSleepMs > 0) {
      try {
        Thread.sleep(config.mNextRequestSleepMs);
      } catch (Throwable ignore) {}
    }
    return chain.proceed(chain.request());
  }
}
