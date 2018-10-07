package com.whensunset.http.model;

/**
 * HttpException表示网络请求正常，但是Response中result不为1.
 */
public class HttpException extends Exception {
  
  public transient final DataContainer<?> mDataContainer;
  
  public final int mErrorCode;
  public final String mErrorMessage;
  
  public HttpException(DataContainer<?> dataContainer) {
    mDataContainer = dataContainer;
    mErrorCode = dataContainer.errorCode();
    mErrorMessage = dataContainer.errorMessage();
  }
  
  @Override
  public String getMessage() {
    return mErrorMessage;
  }
  
  public int getErrorCode() {
    return mErrorCode;
  }
}
