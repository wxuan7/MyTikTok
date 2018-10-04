/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.whensunset.image.okhttp3;

import android.content.Context;

import com.facebook.imagepipeline.core.ImagePipelineConfig;

/**
 * Factory for getting an {@link com.facebook.imagepipeline.core.ImagePipelineConfig} that uses
 * {@link OkHttpNetworkFetcher}.
 */
public class OkHttpImagePipelineConfigFactory {

  public static ImagePipelineConfig.Builder newBuilder(Context context,
                                                       OkHttpClientSupplier supplier) {
    return ImagePipelineConfig.newBuilder(context)
        .setNetworkFetcher(new OkHttpNetworkFetcher(supplier));
  }
}
