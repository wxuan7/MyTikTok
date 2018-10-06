package com.whensunset.image;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.BitmapMemoryCacheKey;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.Postprocessor;

public class MytiktokoImageCacheKeyFactory implements CacheKeyFactory {
  
  @Override
  public CacheKey getBitmapCacheKey(ImageRequest request, Object callerContext) {
    return new BitmapMemoryCacheKey(
        getCacheKeyForRequest(request),
        request.getResizeOptions(),
        request.getRotationOptions(),
        request.getImageDecodeOptions(),
        null,
        null,
        callerContext);
  }
  
  @Override
  public CacheKey getPostprocessedBitmapCacheKey(ImageRequest request, Object callerContext) {
    final Postprocessor postprocessor = request.getPostprocessor();
    final CacheKey postprocessorCacheKey;
    final String postprocessorName;
    if (postprocessor != null) {
      postprocessorCacheKey = postprocessor.getPostprocessorCacheKey();
      postprocessorName = postprocessor.getClass().getName();
    } else {
      postprocessorCacheKey = null;
      postprocessorName = null;
    }
    return new BitmapMemoryCacheKey(
        getCacheKeyForRequest(request),
        request.getResizeOptions(),
        request.getRotationOptions(),
        request.getImageDecodeOptions(),
        postprocessorCacheKey,
        postprocessorName,
        callerContext);
  }
  
  @Override
  public CacheKey getEncodedCacheKey(ImageRequest request, @Nullable Object callerContext) {
    return getEncodedCacheKey(request, request.getSourceUri(), callerContext);
  }
  
  @Override
  public CacheKey getEncodedCacheKey(ImageRequest request, Uri sourceUri,
                                     Object callerContext) {
    return new SimpleCacheKeyWithContext(getCacheKeyForRequest(request), callerContext);
  }
  
  private String getCacheKeyForRequest(ImageRequest request) {
    if (request instanceof MytiktokImageRequest) {
      return ((MytiktokImageRequest) request).getCacheKey();
    }
    return request.getSourceUri().toString();
  }
}
