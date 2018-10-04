package com.whensunset.image;

import android.content.Context;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imageformat.DefaultImageFormatChecker;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ProducerSequenceFactory;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.memory.PoolConfig;
import com.facebook.imagepipeline.memory.PoolFactory;
import com.facebook.imagepipeline.producers.BitmapMemoryCacheGetProducer;
import com.facebook.imagepipeline.producers.BitmapMemoryCacheKeyMultiplexProducer;
import com.facebook.imagepipeline.producers.BitmapMemoryCacheProducer;
import com.facebook.imagepipeline.producers.LocalVideoThumbnailProducer;
import com.facebook.imagepipeline.producers.ThreadHandoffProducer;
import com.whensunset.http.retrofit.utils.ConvertToIOExceptionInterceptor;
import com.whensunset.image.okhttp3.OkHttpClientSupplier;
import com.whensunset.image.okhttp3.OkHttpImagePipelineConfigFactory;
import com.whensunset.logutil.debuglog.DebugLogger;
import com.whensunset.utils.JavaReflectUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;

public class ImageManager {
  private static final String DEBUG_TAG = "DEBUG_TAG";
  
  public static void initialize(Context context) {
    
    Set<RequestListener> listeners = new HashSet<>();
    // todo 可以写一个 日志 listener 来监听 图片加载的整个过程
    try {
      PoolFactory poolFactory = new PoolFactory(PoolConfig.newBuilder().build());
      ImagePipelineConfig config =
          OkHttpImagePipelineConfigFactory.newBuilder(context, new MytiktokOkHttpClientSupplier())
              .setRequestListeners(listeners)
              .setCacheKeyFactory(new MytiktokoImageCacheKeyFactory())
              .setSmallImageDiskCacheConfig(
                  DiskCacheConfig.newBuilder(context).setMaxCacheSize(10 * ByteConstants.MB)
                      .build())
              .setBitmapMemoryCacheParamsSupplier(new MytiktokBitmapMemoryCacheParamsSupplier(context))
              .setIsPrefetchEnabledSupplier(new IsPrefetchEnabledSupplier())
              .setPoolFactory(poolFactory)
              .setDownsampleEnabled(true)
              .build();
      Fresco.initialize(context, config);
    } catch (Throwable t) {
      DebugLogger.e(DEBUG_TAG, "FrescoInitError");
    }
    // workaround for LocalVideoThumbnail
    try {
      ProducerSequenceFactory mProducerSequenceFactory =
          JavaReflectUtil.getFieldOrThrow(Fresco.getImagePipeline(), "mProducerSequenceFactory");
      BitmapMemoryCacheGetProducer bitmapMemoryCacheGetProducer =
          JavaReflectUtil.callMethodOrThrow(mProducerSequenceFactory, "getLocalVideoFileFetchSequence");
      ThreadHandoffProducer threadHandoffProducer =
          JavaReflectUtil.getFieldOrThrow(bitmapMemoryCacheGetProducer, "mInputProducer");
      BitmapMemoryCacheKeyMultiplexProducer bitmapMemoryCacheKeyMultiplexProducer =
          JavaReflectUtil.getFieldOrThrow(threadHandoffProducer, "mInputProducer");
      BitmapMemoryCacheProducer bitmapMemoryCacheProducer =
          JavaReflectUtil.getFieldOrThrow(bitmapMemoryCacheKeyMultiplexProducer, "mInputProducer");
      
      LocalVideoThumbnailProducer localVideoThumbnailProducer =
          JavaReflectUtil.getFieldOrThrow(bitmapMemoryCacheProducer, "mInputProducer");
      
      Executor executor = JavaReflectUtil.getField(localVideoThumbnailProducer, "mExecutor");
      
      JavaReflectUtil.setFieldOrThrow(bitmapMemoryCacheProducer, "mInputProducer",
          new MytiktokLocalVideoThumbnailProducer(executor));
      //https://github.com/facebook/fresco/commit/7b15ae0ed25834a66dd8d478f473b93381ba2c1b
      //在fresco的github上已经正式将文件头的数组改为我们的形式
      JavaReflectUtil.setStaticField(DefaultImageFormatChecker.class, "HEIF_HEADER_SUFFIXES",
          new String[]{"heic", "heix", "hevc", "hevx", "mif1", "msf1"});
    } catch (Throwable e) {
      DebugLogger.e(DEBUG_TAG, "ImageManagerOtherError");
      e.printStackTrace();
    }
  }
  
  static class IsPrefetchEnabledSupplier implements Supplier<Boolean> {
    @Override
    public Boolean get() {
      // todo 之后这里的配置需要存入 preference 中
      return true;
    }
  }
  
  static class MytiktokOkHttpClientSupplier implements OkHttpClientSupplier {
    
    OkHttpClient mHttpClient;
    
    @Override
    public synchronized OkHttpClient get(Priority priority) {
      if (mHttpClient == null) {
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
            .addInterceptor(new ConvertToIOExceptionInterceptor());
        mHttpClient = okHttpClientBuilder.build();
      }
      return mHttpClient;
    }
  }
}
