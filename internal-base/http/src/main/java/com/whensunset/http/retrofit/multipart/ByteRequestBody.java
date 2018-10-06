package com.whensunset.http.retrofit.multipart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;

public class ByteRequestBody extends StreamRequestBody {
  
  private byte[] mContent;
  
  public ByteRequestBody(OnProgressListener progressListener, byte[] content,
                         long start, long size, MediaType mediaType) {
    super(progressListener, content, start, size, mediaType);
    mContent = content;
  }
  
  @Override
  InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(mContent);
  }
}
