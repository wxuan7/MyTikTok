package com.whensunset.image;

import android.support.annotation.NonNull;

import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * 这个Request的目的是让一组Url可以共用一个CacheKey.
 * 例如QPhoto中CDNUrl[]数组创建的ImageRequest[], 逻辑上这组ImageRequest应该共用一个CacheKey.
 */
public class MytiktokImageRequest extends ImageRequest {

  @NonNull
  private final String mCacheKey;

  public MytiktokImageRequest(ImageRequestBuilder builder, @NonNull String cacheKey) {
    super(builder);
    mCacheKey = cacheKey;
  }

  @NonNull
  public String getCacheKey() {
    return mCacheKey;
  }
}
