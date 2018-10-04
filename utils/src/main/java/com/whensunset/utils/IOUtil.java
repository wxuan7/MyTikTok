package com.whensunset.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by whensunset on 2018/10/3.
 */

public class IOUtil {
  
  public static void closeQuietly(OutputStream output) {
    closeQuietly((Closeable) output);
  }
  
  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }
  
  public static boolean copyFile(File srcFile, File dstFile) {
    if (srcFile.exists() && srcFile.isFile()) {
      if (dstFile.isDirectory()) {
        return false;
      }
      if (dstFile.exists()) {
        dstFile.delete();
      }
      try {
        byte[] buffer = new byte[2048];
        BufferedInputStream input = new BufferedInputStream(
            new FileInputStream(srcFile));
        BufferedOutputStream output = new BufferedOutputStream(
            new FileOutputStream(dstFile));
        while (true) {
          int count = input.read(buffer);
          if (count == -1) {
            break;
          }
          output.write(buffer, 0, count);
        }
        input.close();
        output.flush();
        output.close();
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }
}
