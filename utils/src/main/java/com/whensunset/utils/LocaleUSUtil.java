package com.whensunset.utils;

import android.text.TextUtils;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LocaleUSUtil {
  public static String toUpperCase(String str) {
    if (TextUtils.isEmpty(str)) {
      return str;
    }
    
    return str.toUpperCase(Locale.US);
  }
  
  public static String toLowerCase(String str) {
    if (TextUtils.isEmpty(str)) {
      return str;
    }
    
    return str.toLowerCase(Locale.US);
  }
  
  public static DecimalFormat newDecimalFormat(String pattern) {
    return new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US));
  }
  
  public static SimpleDateFormat newSimpleDateFormat(String pattern) {
    return new SimpleDateFormat(pattern, new DateFormatSymbols(Locale.US));
  }
  
  public static String format(String format, Object... args) {
    return String.format(Locale.US, format, args);
  }
}
