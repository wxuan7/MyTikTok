package com.whensunset.utils;

import java.io.File;

/**
 * Created by whensunset on 2018/10/5.
 */

public class FileUtil {
  public static boolean isJpgFile(File file) {
    return file != null && isJpgFile(file.getName());
  }
  
  public static boolean isJpgFile(String filename) {
    return TextUtil.matchFileType(filename, "jpg", "jpeg");
  }
}
