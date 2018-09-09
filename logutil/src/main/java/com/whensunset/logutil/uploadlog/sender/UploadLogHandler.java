package com.whensunset.logutil.uploadlog.sender;

import com.whensunset.logutil.uploadlog.policy.UploadLogPolicy;

/**
 * 处理 Log 发送和重传的机制
 */
public interface UploadLogHandler {

  /**
   * 发送实时日志.
   * 
   * @param batchReportEvent
   */
  void sendRealLog(final Object batchReportEvent);

  void setUploadPolicy(UploadLogPolicy.Upload policy);
  
  void setUploadIntervalMs(long intervalMs);
}
