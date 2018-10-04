package com.whensunset.image;

import com.facebook.cache.common.SimpleCacheKey;

/**
 * Created by whensunset on 2018/10/5.
 */

public class SimpleCacheKeyWithContext extends SimpleCacheKey {
  private final Object mCallerContext;
  
  public SimpleCacheKeyWithContext(String key, Object callerContext) {
    super(key);
    this.mCallerContext = callerContext;
  }
  
  public Object getCallerContext() {
    return this.mCallerContext;
  }
}
