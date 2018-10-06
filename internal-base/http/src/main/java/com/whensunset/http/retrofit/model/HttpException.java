package com.whensunset.http.retrofit.model;

/**
 * HttpException表示网络请求正常，但是Response中result不为1.
 */
public class HttpException extends Exception {
  
  public transient final Response<?> mResponse;
  
  public final int mErrorCode;
  public final String mErrorMessage;
  
  public HttpException(Response<?> response) {
    mResponse = response;
    mErrorCode = response.errorCode();
    mErrorMessage = response.errorMessage();
  }
  
  @Override
  public String getMessage() {
    return mErrorMessage;
  }
  
  public int getErrorCode() {
    return mErrorCode;
  }
}
