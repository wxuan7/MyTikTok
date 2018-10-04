package com.whensunset.http.retrofit;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class DefaultParams implements RetrofitConfig.Params {

  @Override
  @NonNull
  public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<>();
    

    return headers;
  }

  @Override
  @NonNull
  public Map<String, String> getUrlParams() {
    Map<String, String> map = new HashMap<>();
    return map;
  }


  @Override
  @NonNull
  public Map<String, String> getBodyParams() {
    Map<String, String> params = new HashMap<>();
   return params;
  }
}
