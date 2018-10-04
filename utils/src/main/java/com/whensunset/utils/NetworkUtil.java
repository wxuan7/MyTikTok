package com.whensunset.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by whensunset on 2018/10/3.
 */

public class NetworkUtil {
  public static boolean isNetworkConnected(Context context) {
    try {
      ConnectivityManager cm =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (cm == null) {
        return false;
      }
      NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
      return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    } catch (Exception e) {
      return false;
    }
  }
}
