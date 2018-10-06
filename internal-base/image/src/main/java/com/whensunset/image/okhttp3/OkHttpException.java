package com.whensunset.image.okhttp3;

import okhttp3.Response;

public class OkHttpException extends Exception {
  private final int code;
  private final String message;
  
  public OkHttpException(Response response) {
    this.code = response.code();
    this.message = response.message();
  }
  
  public int code() {
    return code;
  }
  
  public String message() {
    return message;
  }
}
