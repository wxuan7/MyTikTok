package com.whensunset.image.okhttp3;

import com.facebook.imagepipeline.common.Priority;

import okhttp3.OkHttpClient;

public interface OkHttpClientSupplier {
  
  OkHttpClient get(Priority priority);
}
