package com.whensunset.http.multipart;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MultipartFactory {
  
  public static final MediaType TEXT_MEDIA_TYPE = MediaType.parse("text/plain");
  public static final MediaType MULTI_PART_MEDIA_TYPE = MediaType.parse("multipart/form-data");
  
  public static Map<String, RequestBody> convertMap2RequestBodyMap(Map<String, String> params) {
    Map<String, okhttp3.RequestBody> map = new HashMap<>();
    Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
    
    while (entries.hasNext()) {
      Map.Entry<String, String> entry = entries.next();
      if (!TextUtils.isEmpty(entry.getValue())) {
        map.put(entry.getKey(), createBodyFromString(entry.getValue()));
      }
    }
    return map;
  }
  
  public static MultipartBody.Part createFileRequestBody(String fileKey, File file, int start,
                                                         long size, OnProgressListener onProgressListener, MediaType mediaType) {
    return MultipartBody.Part
        .createFormData(fileKey, file.getName(), new FileRequestBody(onProgressListener, file,
            start, size, mediaType));
  }
  
  public static MultipartBody.Part createFileRequestBody(String fileKey, File file,
                                                         OnProgressListener onProgressListener) {
    String mime = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(getExtension(file.getName()));
    MediaType mediaType = null;
    if (!TextUtils.isEmpty(mime)) {
      mediaType = MediaType.parse(mime);
    }
    if (mediaType == null) {
      mediaType = MULTI_PART_MEDIA_TYPE;
    }
    return createFileRequestBody(fileKey, file, 0, file.length(), onProgressListener, mediaType);
  }
  
  private static String getExtension(String name) {
    int dotPos = name.lastIndexOf('.');
    if (0 <= dotPos) {
      return name.substring(dotPos + 1);
    }
    return "";
  }
  
  public static MultipartBody.Part createFileRequestBody(String fileKey, File file) {
    return createFileRequestBody(fileKey, file, null);
  }
  
  public static MultipartBody.Part createContentRequestBody(String fileKey, byte[] content,
                                                            String name, OnProgressListener onProgressListener) {
    return MultipartBody.Part
        .createFormData(fileKey, name, new ByteRequestBody(onProgressListener,
            content, 0, content.length, MULTI_PART_MEDIA_TYPE));
  }
  
  public static MultipartBody.Part createContentRequestBody(String fileKey, byte[] content,
                                                            String name) {
    return createContentRequestBody(fileKey, content, name, null);
  }
  
  public static RequestBody createBodyFromString(String content) {
    return RequestBody.create(TEXT_MEDIA_TYPE, content);
  }
  
}
