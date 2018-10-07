package com.whensunset.http.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;


/**
 * 强制将网络请求过程中遇到的非IO异常，全部转换为IO异常，避免崩溃，且方便处理。
 */
public class ConvertToIOExceptionInterceptor implements Interceptor {
  
  @Override
  public Response intercept(Chain chain) throws IOException {
    try {
      return chain.proceed(chain.request());
    } catch (Exception e) {
      if (e instanceof IOException) {
        throw e;
      }
      throw new IOException(e);
    }
  }
}
