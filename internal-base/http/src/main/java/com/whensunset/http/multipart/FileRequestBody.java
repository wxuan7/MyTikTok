package com.whensunset.http.multipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;

/**
 * 上传文件并带回调进度
 */
public final class FileRequestBody extends StreamRequestBody {
  
  private File mFile;
  
  public FileRequestBody(OnProgressListener progressListener, File file,
                         long start, long size, MediaType mediaType) {
    super(progressListener, file, start, size, mediaType);
    mFile = file;
    
  }
  
  @Override
  InputStream getInputStream() throws IOException {
    return new FileInputStream(mFile);
  }
}
