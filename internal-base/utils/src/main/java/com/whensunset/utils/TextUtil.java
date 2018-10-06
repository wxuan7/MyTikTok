package com.whensunset.utils;

import java.io.File;


public class TextUtil {
  public static boolean matchFileType(File file, String... ext) {
    return file != null && matchFileType(file.getName(), ext);
  }
  
  public static boolean matchFileType(String filename, String... suffix) {
    if (android.text.TextUtils.isEmpty(filename)) {
      return false;
    }
    filename = LocaleUSUtil.toLowerCase(filename);
    for (String s : suffix) {
      if (filename.endsWith(LocaleUSUtil.toLowerCase(s))) {
        return true;
      }
    }
    return false;
  }
  
}
