package com.whensunset.http;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.annotation.Annotation;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import okhttp3.OkHttpClient;
import retrofit2.Call;

public interface RetrofitConfig {
  
  Gson buildGson();
  
  Params buildParams();
  
  String buildBaseUrl();
  
  OkHttpClient buildClient();
  
  Call<Object> buildCall(Call<Object> call);
  
  Observable<?> buildObservable(Observable<?> o, Call<Object> call, Annotation[] annotations);
  
  Scheduler getExecuteScheduler();
  
  
  interface Params {
    
    @NonNull
    Map<String, String> getHeaders();
    
    @NonNull
    Map<String, String> getUrlParams();
    
    @NonNull
    Map<String, String> getBodyParams();
  }
}
