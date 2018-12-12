package com.whensunset.logutil.locallog;

/**
 */
public class StringBuilderHolder {
  
  // 公共的Holder
  private static ThreadLocal<StringBuilder> globalStringBuilder = new ThreadLocal<StringBuilder>() {
    @Override
    protected StringBuilder initialValue() {
      return new StringBuilder(512);
    }
  };
  private int initSize;
  // 独立的Holder
  private ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
    @Override
    protected StringBuilder initialValue() {
      return new StringBuilder(initSize);
    }
  };
  
  
  public StringBuilderHolder(int initSize) {
    this.initSize = initSize;
  }
  
  
  public StringBuilderHolder() {
    this(512);
  }
  
  public static StringBuilder getGlobal() {
    StringBuilder sb = globalStringBuilder.get();
    sb.setLength(0);
    return sb;
  }
  
  public StringBuilder get() {
    StringBuilder sb = stringBuilder.get();
    sb.setLength(0);
    return sb;
  }
}
