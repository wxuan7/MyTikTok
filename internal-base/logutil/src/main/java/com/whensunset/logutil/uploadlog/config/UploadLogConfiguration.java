package com.whensunset.logutil.uploadlog.config;

public interface UploadLogConfiguration {
  
  
  /**
   * Log 发送失败后，客户端保留的时间.
   *
   * @return
   */
  long getLogRetentionTime();
  
  
  /**
   * 日志发送是否使用debug间隔(3s)
   *
   * @return
   */
  boolean useDebugSendingInterval();
  
  
  /**
   * Log 最多失败的次数。
   * @return
   */
  int getMaxFailedCount();
  
}
