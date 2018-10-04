package com.whensunset.http.retrofit.utils;

import android.util.Pair;

import com.whensunset.http.retrofit.RetrofitConfig;

import java.util.Map;

public class ParamsUtils {

  public static Pair<Map<String, String>, Map<String, String>> obtainParams(
      RetrofitConfig.Params config, Map<String, String> params,
      boolean get) {
    Map<String, String> urlParams = config.getUrlParams();
    Map<String, String> bodyParams = config.getBodyParams();
    
    if (params != null) {
      if (get) {
        urlParams.putAll(params);
      } else {
        bodyParams.putAll(params);
      }
    }
    escapeParams(urlParams, bodyParams);
    // 如果是Get请求, 则把所有post参数全部放到urlParams里去
    if (get) {
      urlParams.putAll(bodyParams);
      bodyParams.clear();
    }

    return new Pair<>(urlParams, bodyParams);
  }

  // 对一些参数做修正, 目前仅处理了空参数
  private static void escapeParams(Map<String, String> urlParams, Map<String, String> bodyParams) {
    if (urlParams != null) {
      for (String key : urlParams.keySet()) {
        String value = urlParams.get(key);
        if (value == null) {
          value = "";
          urlParams.put(key, value);
        }
      }
    }

    if (bodyParams != null) {
      for (String key : bodyParams.keySet()) {
        String value = bodyParams.get(key);
        if (value == null) {
          value = "";
          bodyParams.put(key, value);
        }
      }
    }
  }
}
