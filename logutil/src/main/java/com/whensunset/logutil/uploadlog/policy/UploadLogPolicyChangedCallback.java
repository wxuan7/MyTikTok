package com.whensunset.logutil.uploadlog.policy;

public interface UploadLogPolicyChangedCallback {
  void onLogPolicyChanged(UploadLogPolicy policy);
  void onSendIntervalChanged(long interval);
}
