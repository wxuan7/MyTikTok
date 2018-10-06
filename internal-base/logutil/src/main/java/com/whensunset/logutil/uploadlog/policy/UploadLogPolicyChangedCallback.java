package com.whensunset.logutil.uploadlog.policy;

public interface UploadLogPolicyChangedCallback {
  // 修改发送日志策略的回调
  void onLogPolicyChanged(UploadLogPolicy policy);
  
  // 修改发送日志的时间间隔
  void onSendIntervalChanged(long interval);
}
