package com.whensunet.core.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.whensunet.core.MytiktokApp;


final public class PreferenceUtil {
  private static SharedPreferences sSharedPreferences;
  
  static {
    sSharedPreferences = MytiktokApp.getAppContext().getSharedPreferences("DefaultPreferenceHelper", Context.MODE_PRIVATE);
  }
  
  private PreferenceUtil() {
  }
  
  public static SharedPreferences getPreferences() {
    return sSharedPreferences;
  }
}
