package com.whensunset.logutil.uploadlog.sender;


import java.util.Map;

//  todo 未来确定了网络框架之后 需要实现一个 log 上传类
public interface LogSender {
  
  boolean send(Object reportEvent, Map<String, String> params);
  
}
