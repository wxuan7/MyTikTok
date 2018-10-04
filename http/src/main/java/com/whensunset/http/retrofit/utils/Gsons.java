package com.whensunset.http.retrofit.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 */
public final class Gsons {

  public static final Gson RAW_GSON =
      new GsonBuilder().serializeSpecialFloatingPointValues()
          .disableHtmlEscaping()
          .serializeSpecialFloatingPointValues()
          .create();

  // todo 后续需要定制 gson
  public static final Gson GSON = new GsonBuilder().create();
}
