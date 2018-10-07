package com.whensunset.http.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class SSLFactoryInterceptor implements Interceptor {
  // todo 还没有后端 这个暂时放着
  @Override
  public Response intercept(Chain chain) throws IOException {
    return chain.proceed(chain.request());
  }
}
