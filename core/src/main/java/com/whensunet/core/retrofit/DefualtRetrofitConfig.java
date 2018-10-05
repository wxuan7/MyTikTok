package com.whensunet.core.retrofit;

import com.google.gson.Gson;
import com.whensunset.http.retrofit.DefaultParams;
import com.whensunset.http.retrofit.RetrofitConfig;
import com.whensunset.http.retrofit.consumer.NetworkCounter;
import com.whensunset.http.retrofit.interceptor.ConfigParamsInterceptor;
import com.whensunset.http.retrofit.interceptor.SSLFactoryInterceptor;
import com.whensunset.http.retrofit.interceptor.TimeoutInterceptor;
import com.whensunset.http.retrofit.interceptor.throttling.ThrottlingConsumer;
import com.whensunset.http.retrofit.interceptor.throttling.ThrottlingInterceptor;
import com.whensunset.http.retrofit.utils.Gsons;
import com.whensunset.http.retrofit.utils.RetrofitSchedulers;
import com.whensunset.logutil.networklog.HttpLoggingInterceptor;

import java.lang.annotation.Annotation;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import okhttp3.OkHttpClient;
import retrofit2.Call;

public class DefualtRetrofitConfig implements RetrofitConfig {
  
  public static final int DEFAULT_UPLOAD_TIMEOUT_S = 60;
  public static final int DEFAULT_TIMEOUT_S = 15;
  private static final Random RANDOM = new Random();
  
  
  private static OkHttpClient sApiClient;
  private static OkHttpClient sUploadClient;
  // todo 需要服务器下发，每次进入 app 储存到 preferences 中，api重试次数
  private static int sApiRetryTimes = 10;
  
  // todo 目前还没实现重试逻辑，sApiRetryTimes 0~10才允许重试，太大不合理
  private final boolean mRetryTimesValid;
  private final Scheduler mScheduler;
  // 文件上传 和 普通 api 的逻辑不同
  private boolean isUpload = false;
  
  public DefualtRetrofitConfig(Scheduler scheduler) {
    mScheduler = scheduler;
    mRetryTimesValid = sApiRetryTimes > 0 && sApiRetryTimes <= 10;
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
        .addInterceptor(new SSLFactoryInterceptor())
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
    return call;
  }
  
  @Override
  public Observable<?> buildObservable(Observable<?> input, Call<Object> call,
                                       Annotation[] annotations) {
    // 先切到主线程
    Observable<?> observable = input.observeOn(RetrofitSchedulers.MAIN)
        .doOnComplete(NetworkCounter.ON_COMPLETE)
        .doOnError(NetworkCounter.ON_ERROR)
        // 处理Throttling(防止ddos)
        .doOnNext(new ThrottlingConsumer());
    
    return observable;
  }
}
