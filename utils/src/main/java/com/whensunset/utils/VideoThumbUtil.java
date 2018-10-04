package com.whensunset.utils;

import java.io.File;

public class VideoThumbUtil {

  public static File getJpgFile(File originFile) {
    if (FileUtil.isJpgFile(originFile)) {
      return originFile;
    } else {
      String jpgFilePath = originFile.getAbsolutePath().hashCode() + ".jpg";
      return new File(jpgFilePath);
    }
  }

}
