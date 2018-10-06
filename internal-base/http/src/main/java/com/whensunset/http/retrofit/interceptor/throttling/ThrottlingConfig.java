package com.whensunset.http.retrofit.interceptor.throttling;

public class ThrottlingConfig {
  
  public final long mPolicyExpireMs;
  public final long mNextRequestSleepMs;
  
  public ThrottlingConfig(long policyExpireMs, long nextRequestSleepMs) {
    mPolicyExpireMs = System.currentTimeMillis() + policyExpireMs;
    mNextRequestSleepMs = nextRequestSleepMs;
  }
}
