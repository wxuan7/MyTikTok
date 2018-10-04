package com.whensunset.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BitmapUtil {

  public static final int HIGH_QUALITY_PAINT_FLAG =
      Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG;
  public static final Paint BITMAP_PAINT = new Paint(HIGH_QUALITY_PAINT_FLAG);

  public static ByteBuffer toByteBuffer(Bitmap bitmap, boolean allocateDirect) {
    ByteBuffer argbByteBuffer = allocateDirect
        ? ByteBuffer.allocateDirect(bitmap.getWidth() * bitmap.getHeight() * 4)
        : ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 4);
    int[] argbIntArray = toIntArray(bitmap);
    argbByteBuffer.asIntBuffer().put(argbIntArray);
    return argbByteBuffer;
  }

  public static int[] toIntArray(Bitmap bitmap) {
    int[] argbIntArray = new int[bitmap.getWidth() * bitmap.getHeight()];
    bitmap.getPixels(argbIntArray, 0, bitmap.getWidth(), 0, 0,
        bitmap.getWidth(), bitmap.getHeight());
    return argbIntArray;
  }

  /**
   * 读取图片属性：旋转的角度
   */
  public static int readPictureDegree(String path) {
    int degree = 0;
    try {
      ExifInterface exifInterface = new ExifInterface(path);
      int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_NORMAL);
      switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
          degree = 90;
          break;
        case ExifInterface.ORIENTATION_ROTATE_180:
          degree = 180;
          break;
        case ExifInterface.ORIENTATION_ROTATE_270:
          degree = 270;
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return degree;
  }

  public static byte[] getBitmapBytes(@NonNull Bitmap bitmap) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.JPEG, 98, bos);
    byte[] bytes = bos.toByteArray();
    IOUtil.closeQuietly(bos);
    return bytes;
  }

  public static void saveToFileUseJPEG(Bitmap bitmap, String filePath, int quality)
      throws IOException {
    CompressFormat fmt = CompressFormat.JPEG;
    FileOutputStream os = new FileOutputStream(filePath);
    try {
      bitmap.compress(fmt, quality, os);
    } finally {
      IOUtil.closeQuietly(os);
    }
  }

  public static Bitmap createBitmapFromView(View view) {
    int visible = view.getVisibility();
    view.setVisibility(View.VISIBLE);
    Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
        view.getMeasuredHeight(), Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    view.setVisibility(visible);
    return bitmap;
  }

  /**
   * Make bitmap mutable, scaled to size and matched the format
   *
   * @param bitmap Source bitmap
   * @param width Target width
   * @param height Target height
   * @param config Target format
   * @return
   */
  public static Bitmap scale(Bitmap bitmap, int width, int height, Config config) {
    return scale(bitmap, width, height, config, true);
  }

  public static Bitmap scale(Bitmap bitmap, int maxSize, Config config) {
    if (bitmap == null) {
      return null;
    }

    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    if (maxSize <= 0 || Math.max(width, height) <= maxSize) {
      if (config == null || bitmap.getConfig().equals(config)) {
        return bitmap;
      }
    }

    if (maxSize > 0 && width > height && width > maxSize) {
      height = height * maxSize / width;
      width = maxSize;
    } else if (maxSize > 0 && height > width && height > maxSize) {
      width = width * maxSize / height;
      height = maxSize;
    }

    if (width != bitmap.getWidth() || height != bitmap.getHeight()) {
      bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    if (config == null) {
      config = bitmap.getConfig();
    }
    if (!bitmap.isMutable() || !bitmap.getConfig().equals(config)) {
      bitmap = bitmap.copy(config, true);
    }
    return bitmap;
  }

  /**
   * Make bitmap mutable, scaled to size and matched the format
   *
   * @param bitmap Source bitmap
   * @param width Target width
   * @param height Target height
   * @param config Target format
   * @param isRecyclingOriginalBitmap if the original bitmap should be recycled or not
   * @return
   */
  public static Bitmap scale(Bitmap bitmap, int width, int height, Config config,
      boolean isRecyclingOriginalBitmap) {
    if (bitmap == null) {
      return null;
    }
    if (config == null) {
      config = bitmap.getConfig();
    }
    boolean isNewBitmapCreated = false;
    if (width != bitmap.getWidth() || height != bitmap.getHeight()) {
      Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
      isNewBitmapCreated = true;
      if (isRecyclingOriginalBitmap) {
        bitmap.recycle();
      }
      bitmap = scaled;
    }
    if (!bitmap.isMutable() || !bitmap.getConfig().equals(config)) {
      Bitmap copyed = bitmap.copy(config, true);
      if (isNewBitmapCreated || isRecyclingOriginalBitmap) {
        bitmap.recycle();
      }
      bitmap = copyed;
    }
    return bitmap;
  }

  public static Bitmap crop(Bitmap bitmap, int targetWidth, int targetHeight) {
    return crop(bitmap, targetWidth, targetHeight, BitmapCropMode.CENTER);
  }

  public static Bitmap crop(Bitmap bitmap, int targetWidth, int targetHeight, BitmapCropMode mode) {
    int width = bitmap.getWidth(), height = bitmap.getHeight();
    if (width == targetWidth && height == targetHeight) {
      return bitmap.copy(bitmap.getConfig(), true);
    }
    Bitmap cropped = Bitmap.createBitmap(targetWidth, targetHeight,
        bitmap.getConfig() == null
            ? Config.ARGB_8888
            : bitmap.getConfig());
    if (cropped == null) {
      return null;
    }
    Canvas canvas = new Canvas(cropped);
    Rect rect = null;
    if (width * targetHeight > height * targetWidth) {
      int newWidth = height * targetWidth / targetHeight;
      switch (mode) {
        case TOP:
          rect = new Rect(0, 0, newWidth, height);
          break;
        case CENTER:
          rect = new Rect((width - newWidth) / 2, 0, (width + newWidth) / 2, height);
          break;
        case BOTTOM:
          rect = new Rect(width - newWidth, 0, width, height);
          break;
        default:
          break;
      }
    } else if (width * targetHeight < height * targetWidth) {
      int newHeight = width * targetHeight / targetWidth;

      switch (mode) {
        case TOP:
          rect = new Rect(0, 0, width, newHeight);
          break;
        case CENTER:
          rect = new Rect(0, (height - newHeight) / 2, width, (height + newHeight) / 2);
          break;
        case BOTTOM:
          rect = new Rect(0, height - newHeight, width, height);
          break;
        default:
          break;
      }
    }
    canvas.drawBitmap(bitmap, rect, new Rect(0, 0, targetWidth, targetHeight), BITMAP_PAINT);
    return cropped;
  }

  public static void reduceImageFileSize(File file, int maxSize, int minQuality) {
    if (file == null || !file.exists() || !file.canWrite() || !file.canRead()) {
      return;
    }
    if (file.length() <= maxSize || maxSize <= 0) {
      return;
    }
    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
    if (bitmap == null) {
      return;
    }
    int quality = getPreferQuality(bitmap, maxSize, minQuality);
    if (quality > 100 || quality < 0) {
      return;
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      bitmap.compress(CompressFormat.JPEG, quality, fos);
    } catch (Exception e) {
    } finally {
      bitmap.recycle();
      IOUtil.closeQuietly(fos);
    }
  }

  private static int getPreferQuality(Bitmap bitmap, int maxSize, int minQuality) {
    if (maxSize <= 0) {
      return 100;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int quality = 100;
    bitmap.compress(CompressFormat.JPEG, quality, baos);
    while (baos.toByteArray().length > maxSize) {
      baos.reset();
      quality -= 10;
      if (quality <= minQuality) {
        quality = minQuality;
        break;
      }
      bitmap.compress(CompressFormat.JPEG, quality, baos);
    }
    IOUtil.closeQuietly(baos);
    return quality;
  }

  public static Bitmap createCircleImage(Bitmap source) {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    int w = source.getWidth();
    int h = source.getHeight();
    Bitmap target = Bitmap.createBitmap(w, h, Config.ARGB_8888);
    Canvas canvas = new Canvas(target);
    canvas.drawCircle(w / 2, h / 2, Math.min(w / 2, h / 2), paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(source, 0, 0, paint);
    return target;
  }

  /**
   * 水平/垂直翻转图片
   *
   * @param source 原始图片
   * @param horizontal true：水平翻转 false:垂直翻转
   * @return 翻转后的图片
   */
  public static Bitmap flip(@NonNull Bitmap source, boolean horizontal) {
    Matrix matrix = new Matrix();
    if (horizontal) {
      matrix.postScale(-1, 1);
    } else {
      matrix.postScale(1, -1);
    }
    return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
  }

  /**
   * safety call of {@link Bitmap#createBitmap(int, int, Config)} if config is one of
   * {@link Config#ARGB_4444} or {@link Config#ARGB_8888} and OutOfMemoery is caught, we will change
   * config to {@link Config#RGB_565}
   *
   * @param width
   * @param height
   * @param config
   * @return null if there is no memery for the bitmap
   * @author yangkai@wandoujia.com
   */
  public static Bitmap createBitmapSafty(int width, int height, Config config) {
    Bitmap output = null;
    try {
      output = Bitmap.createBitmap(width, height, config);
    } catch (Throwable e) {
      if (e instanceof OutOfMemoryError && config == Config.ARGB_8888) {
        return createBitmapSafty(width, height, Config.RGB_565);
      }
    }
    return output;
  }

  public static void recycleQuietly(Bitmap b) {
    if (b != null && !b.isRecycled()) {
      b.recycle();
    }
  }

  public static boolean isValid(Bitmap b) {
    return b != null && !b.isRecycled();
  }
  
  
  /**
   * 按比例缩放图片
   *
   * @param origin 原图
   * @param ratio  比例
   * @return 新的bitmap
   */
  public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
    if (origin == null) {
      return null;
    }
    int width = origin.getWidth();
    int height = origin.getHeight();
    Matrix matrix = new Matrix();
    matrix.preScale(ratio, ratio);
    Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
    if (newBM.equals(origin)) {
      return newBM;
    }
    origin.recycle();
    return newBM;
  }

  public enum BitmapCropMode {
    TOP, CENTER, BOTTOM
  }
}
