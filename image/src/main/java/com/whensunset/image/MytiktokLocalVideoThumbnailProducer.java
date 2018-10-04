/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.whensunset.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.facebook.common.internal.ImmutableMap;
import com.facebook.common.internal.VisibleForTesting;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.SimpleBitmapReleaser;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.producers.StatefulProducerRunnable;
import com.facebook.imagepipeline.request.ImageRequest;
import com.whensunset.utils.VideoThumbUtil;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * A producer that creates video thumbnails.
 *
 * <p>
 * At present, these thumbnails are created on the java heap rather than being pinned
 * purgeables. This is deemed okay as the thumbnails are only very small.
 */
public class MytiktokLocalVideoThumbnailProducer
    implements
    Producer<CloseableReference<CloseableImage>> {

  private static final int COMPRESS_QUALITY = 98;
  private static final int THUMBNAIL_DEFAULT_SIZE = 1024;
  private static final int THUMBNAIL_MICRO_THRESHOLD = 96;

  @VisibleForTesting
  static final String PRODUCER_NAME = "VideoThumbnailProducer";
  @VisibleForTesting
  static final String CREATED_THUMBNAIL = "createdThumbnail";

  private final Executor mExecutor;
  private static final Set<String> sFileWritingPathSet =
      Collections.synchronizedSet(new HashSet<>());

  public MytiktokLocalVideoThumbnailProducer(Executor executor) {
    mExecutor = executor;
  }

  private static int calculateKind(ImageRequest imageRequest) {
    if (imageRequest.getPreferredWidth() > THUMBNAIL_MICRO_THRESHOLD
        || imageRequest.getPreferredHeight() > THUMBNAIL_MICRO_THRESHOLD) {
      return MediaStore.Images.Thumbnails.MINI_KIND;
    }
    return MediaStore.Images.Thumbnails.MICRO_KIND;
  }

  private static Bitmap getThumbBitmapByDecode(@NonNull ImageRequest imageRequest) {
    final int requestWidth = imageRequest.getPreferredWidth() > 0
        ? Math.min(imageRequest.getPreferredWidth(), THUMBNAIL_DEFAULT_SIZE)
        : THUMBNAIL_DEFAULT_SIZE;
    final int requestHeight = imageRequest.getPreferredHeight() > 0
        ? Math.min(imageRequest.getPreferredHeight(), THUMBNAIL_DEFAULT_SIZE)
        : THUMBNAIL_DEFAULT_SIZE;
    File savedThumbFile = VideoThumbUtil.getJpgFile(imageRequest.getSourceFile());
    Bitmap bitmap = null;
    // 如果缓存文件在写，就不去读，否则会出现花屏
    if (!sFileWritingPathSet.contains(savedThumbFile.getAbsolutePath())
        && savedThumbFile.exists()) {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.outHeight = requestHeight;
      options.outWidth = requestWidth;
      
      bitmap = BitmapFactory.decodeFile(savedThumbFile.getAbsolutePath(), options);
    }

    // 再从视频里面解一个出来
    boolean shouldRewriteFile = false;
    if (bitmap == null) {
      shouldRewriteFile = true;
      // todo 还没写如何从 视频中解一个预览图片
    }
    // 失败了从系统缩略图里读取
    if (bitmap == null) {
      bitmap = ThumbnailUtils.createVideoThumbnail(imageRequest.getSourceFile().getPath(),
          calculateKind(imageRequest));
    }
    if (bitmap == null) {
      return null;
    }

    if (!sFileWritingPathSet.contains(savedThumbFile.getAbsolutePath())) {
      sFileWritingPathSet.add(savedThumbFile.getAbsolutePath());
      if (shouldRewriteFile) {
        // todo 把得到的bitmap存一下，不要每次都写，不然多次写入质量会超低
      }
    }
    sFileWritingPathSet.remove(savedThumbFile.getAbsolutePath());
    return bitmap;
  }

  /**
   * 为加快速度，优先使用系统提供API,系统API获取为空时再使用 BitmapUtil 提供API
   */
  private static Bitmap getThumbBitmapBySystem(@NonNull ImageRequest imageRequest) {
    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(
        imageRequest.getSourceFile().getAbsolutePath(),
        calculateKind(imageRequest));
    if (bitmap == null) {
      bitmap = getThumbBitmapByDecode(imageRequest);
    }
    return bitmap;
  }

  @Override
  public void produceResults(
      final Consumer<CloseableReference<CloseableImage>> consumer,
      final ProducerContext producerContext) {

    final ProducerListener listener = producerContext.getListener();
    final String requestId = producerContext.getId();
    final ImageRequest imageRequest = producerContext.getImageRequest();
    final StatefulProducerRunnable cancellableProducerRunnable =
        new StatefulProducerRunnable<CloseableReference<CloseableImage>>(
            consumer,
            listener,
            PRODUCER_NAME,
            requestId) {

          @Override
          protected CloseableReference<CloseableImage> getResult() {
            Bitmap bitmap = null;
            if (imageRequest != null) {
              // todo 之后存到 preference 中
              boolean isDisableSystemThumbnail = false;
              bitmap = !isDisableSystemThumbnail
                  ? getThumbBitmapBySystem(imageRequest)
                  : getThumbBitmapByDecode(imageRequest);
            }
            return bitmap == null
                ? null
                : CloseableReference.of(
                new CloseableStaticBitmap(
                    bitmap,
                    SimpleBitmapReleaser.getInstance(),
                    ImmutableQualityInfo.FULL_QUALITY,
                    0));
          }

          @Override
          protected Map<String, String> getExtraMapOnSuccess(
              final CloseableReference<CloseableImage> result) {
            return ImmutableMap.of(CREATED_THUMBNAIL, String.valueOf(result != null));
          }

          @Override
          protected void disposeResult(CloseableReference<CloseableImage> result) {
            CloseableReference.closeSafely(result);
          }
        };
    producerContext.addCallbacks(
        new BaseProducerContextCallbacks() {
          @Override
          public void onCancellationRequested() {
            cancellableProducerRunnable.cancel();
          }
        });
    mExecutor.execute(cancellableProducerRunnable);
  }
}
