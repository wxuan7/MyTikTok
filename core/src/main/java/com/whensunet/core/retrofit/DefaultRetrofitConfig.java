package com.whensunet.core.retrofit;

import com.google.gson.Gson;
import com.whensunset.http.DefaultParams;
import com.whensunset.http.RetrofitConfig;
import com.whensunset.http.consumer.SuccessAndFailureCounter;
import com.whensunset.http.interceptor.ConfigParamsInterceptor;
import com.whensunset.http.interceptor.TimeoutInterceptor;
import com.whensunset.http.interceptor.throttling.ThrottlingConsumer;
import com.whensunset.http.interceptor.throttling.ThrottlingInterceptor;
import com.whensunset.http.model.DataContainerCall;
import com.whensunset.http.utils.Gsons;
import com.whensunset.http.utils.RetrofitSchedulers;
import com.whensunset.logutil.networklog.HttpLoggingInterceptor;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import okhttp3.OkHttpClient;
import retrofit2.Call;

public class DefaultRetrofitConfig implements RetrofitConfig {
  
  public static final int DEFAULT_UPLOAD_TIMEOUT_S = 60;
  public static final int DEFAULT_TIMEOUT_S = 15;
  
  private static OkHttpClient sApiClient;
  private static OkHttpClient sUploadClient;
  
  private final Scheduler mScheduler;
  // 文件上传 和 普通 api 的逻辑不同
  private boolean isUpload = false;
  
  public DefaultRetrofitConfig(Scheduler scheduler) {
    mScheduler = scheduler;
  }
  
  public static OkHttpClient getClient() {
    return sApiClient;
  }
  
  @Override
  public Scheduler getExecuteScheduler() {
    return mScheduler;
  }
  
  @Override
  public Gson buildGson() {
    return Gsons.GSON;
  }
  
  @Override
  public Params buildParams() {
    return new DefaultParams();
  }
  
  @Override
  public String buildBaseUrl() {
    return "";
  }
  
  private OkHttpClient.Builder createOkHttpClientBuilder(int timeout) {
    
    return new OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .addInterceptor(new HttpLoggingInterceptor())
        .addInterceptor(new ThrottlingInterceptor())
        .addInterceptor(new TimeoutInterceptor())
        .addInterceptor(new ConfigParamsInterceptor(buildParams()));
  }
  
  @Override
  public OkHttpClient buildClient() {
    if (isUpload) {
      if (sUploadClient == null) {
        sUploadClient = createOkHttpClientBuilder(DEFAULT_UPLOAD_TIMEOUT_S).build();
      }
      return sUploadClient;
    }
    
    if (sApiClient == null) {
      OkHttpClient.Builder apiClientBuilder = createOkHttpClientBuilder(DEFAULT_TIMEOUT_S);
      sApiClient = apiClientBuilder.build();
    }
    
    return sApiClient;
  }
  
  @Override
  public Call<Object> buildCall(Call<Object> call) {
    return new DataContainerCall<>(call);
  }
  
  @Override
  public Observable<?> buildObservable(Observable<?> input, Call<Object> call,
                                       Annotation[] annotations) {
    // 先切到主线程
    Observable<?> observable = input.observeOn(RetrofitSchedulers.MAIN)
        .doOnComplete(SuccessAndFailureCounter.ON_COMPLETE)
        .doOnError(SuccessAndFailureCounter.ON_ERROR)
        // 处理Throttling(防止ddos)
        .doOnNext(new ThrottlingConsumer());
    
    return observable;
  }
}
