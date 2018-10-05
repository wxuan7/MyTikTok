package com.whensunset.logutil.uploadlog.storage;


import com.whensunset.logutil.uploadlog.policy.UploadLogPolicy;

// todo 目前所有方法里的 Object 在未来都会被替换成具体的 log 容器类
public interface UploadLogStorage {
  
  /**
   * 添加一条 Log 到存储中
   *
   * @param reportEvent
   */
  long addLog(Object reportEvent);
  
  /**
   * 获取一条 Log.
   *
   * @param id
   * @return
   */
  Object getLog(long id);
  
  /**
   * 发送失败的时候，记录失败的日志.
   *
   * @param id
   */
  void updateFailureLog(long id);
  
  /**
   * 删除一条 LOG
   *
   * @param id
   */
  void deleteLog(long id);
  
  /**
   * 批量删除 log
   *
   * @param reportEvents
   */
  void deleteLog(Object reportEvents);
  
  /**
   * 获取 N 条普通日志.
   *
   * @param logNum
   * @return
   */
  Object[] getLogs(int logNum);
  
  /**
   * 获取 N 条延时日志.
   *
   * @param logNum
   * @return wrapped client log array
   */
  Object[] getDelayedLogs(int logNum);
  
  /**
   * 获取全部的日志.
   *
   * @return
   */
  Object[] getLogs();
  
  /**
   * 获取当前日志的发送失败的时间.
   *
   * @param id
   * @return
   */
  long getFailedTime(long id);
  
  /**
   * 获取当前 id 的失败次数.
   *
   * @param id
   * @return
   */
  int getFailedCount(long id);
  
  /**
   * 删除所有的 Log.
   */
  void deleteAll();
  
  /**
   * 获取 {@link logId} 之前 Log
   *
   * @param logId
   */
  Object[] getLogBelowId(long logId);
  
  /**
   * 设置Log持久化的策略，普通、延时或丢弃.
   */
  void setSavePolicy(UploadLogPolicy.Save policy);
}
