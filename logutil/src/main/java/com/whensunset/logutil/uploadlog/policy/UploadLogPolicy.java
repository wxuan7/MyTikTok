package com.whensunset.logutil.uploadlog.policy;

import com.google.gson.annotations.SerializedName;

/**
 * 日志存储发送的Policy.
 */
public enum UploadLogPolicy {
  DEFAULT(Save.DEFAULT, Upload.NORMAL),
  @SerializedName("NORMAL")NORMAL(Save.DEFAULT, Upload.ALL),
  @SerializedName("DELAY")DELAY(Save.DELAY, Upload.NONE),
  @SerializedName("DISCARD")DROP(Save.DROP, Upload.NONE);
  
  private Save mSavePolicy;
  private Upload mUploadPolicy;
  
  UploadLogPolicy(Save savePolicy, Upload uploadPolicy) {
    mSavePolicy = savePolicy;
    mUploadPolicy = uploadPolicy;
  }

  public Save getSavePolicy() {
    return mSavePolicy;
  }
  
  public Upload getUploadPolicy() {
    return mUploadPolicy;
  }
  
  public enum Save {
    DEFAULT, // 日志标记为正常的
    DELAY,   // 日志标记为延时日志
    DROP     // 不存储日志，直接丢弃
  }
  
  public enum Upload {
    NORMAL,     // 发送普通日志
    ALL,     // 同时发送普通日志和延时日志
    NONE     // 不发送日志
  }
}
