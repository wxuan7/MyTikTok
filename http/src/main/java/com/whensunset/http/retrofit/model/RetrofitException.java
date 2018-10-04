package com.whensunset.http.retrofit.model;

import java.io.IOException;

import okhttp3.Request;

public class RetrofitException extends IOException {

  public final Request mRequest;
  public final Exception mCause;
  public final int mResponseCode;
  public final String mExpiresTime;

  public RetrofitException(Exception exception, Request request, int responseCode,
      String expiresTime) {
    mRequest = request;
    mCause = exception;
    mResponseCode = responseCode;
    mExpiresTime = expiresTime;
  }

  @Override
  public synchronized Throwable getCause() {
    return mCause;
  }
}
