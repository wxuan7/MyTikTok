package com.whensunset.http.retrofit.multipart;

import com.whensunset.utils.IOUtil;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public abstract class StreamRequestBody extends RequestBody {

  private static final int BUFFER_SIZE = 4096;

  private final OnProgressListener mProgressListener;
  private long mStart;
  private long mEnd;
  private MediaType mMediaType;
  private Object mObject;

  public StreamRequestBody(OnProgressListener progressListener, Object object,
                           long start, long size, MediaType mediaType) {
    mProgressListener = progressListener;
    mStart = start;
    mEnd = start + size;
    mMediaType = mediaType;
    mObject = object;
  }

  @Override
  public long contentLength() throws IOException {
    return mEnd - mStart;
  }

  @Override
  public MediaType contentType() {
    return mMediaType;
  }

  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    InputStream in = getInputStream();
    try {
      in.skip(mStart);
      int length;
      int total = (int) contentLength();
      int bytesRead = 0;
      while ((length = in.read(buffer, 0, Math.min(buffer.length, total - bytesRead))) > 0) {
        sink.write(buffer, 0, length);
        bytesRead += length;
        if (mProgressListener != null && mProgressListener.onProgress(bytesRead, total, mObject)) {
          throw new UploadCancelledException();
        }
      }
    } finally {
      IOUtil.closeQuietly(in);
    }
  }

  abstract InputStream getInputStream() throws IOException;

}
