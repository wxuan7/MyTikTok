package com.whensunset.http.retrofit.interceptor.throttling;

import java.util.HashMap;
import java.util.Map;

public class ThrottlingConfigHolder {

  public static ThrottlingConfigHolder sDefault;

  private final Map<String, ThrottlingConfig> mThrottlingConfigs = new HashMap<>();

  private ThrottlingConfigHolder() {
    // single
  }

  public synchronized static ThrottlingConfigHolder getDefault() {
    if (sDefault == null) {
      sDefault = new ThrottlingConfigHolder();
    }
    return sDefault;
  }

  public synchronized void put(String path, ThrottlingConfig config) {
    mThrottlingConfigs.put(path, config);
  }

  public synchronized ThrottlingConfig remove(String path) {
    return mThrottlingConfigs.remove(path);
  }
}
