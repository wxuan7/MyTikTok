package com.whensunset.logutil.uploadlog.config;

import android.location.Location;

import com.whensunset.logutil.uploadlog.policy.UploadLogPolicyChangedCallback;
import com.whensunset.logutil.uploadlog.sender.LogSender;

public interface UploadLogConfiguration {

  /**
   * 渠道，返回当前的 渠道即可.
   *
   * @return
   */
  String getChannel();

  /**
   * 版本名称
   *
   * @return
   */
  String getVersionName();

  /**
   * 版本号
   *
   * @return
   */
  int getVersionCode();

  /**
   * 获取热更新的PatchVersion
   */
  String getPatchVersion();

  /**
   * 设备的唯一 Id
   *
   * @return
   */
  String getDeviceId();

  /**
   * 用户的 id, 如果没有的返回 null
   *
   * @return
   */
  Long getUserId();

  /**
   * 用户标识，客户端透传
   */
  String getUserFlag();

  /**
   * 当前应用所占的磁盘使用量, 单位是 MB.
   *
   * @return
   */
  int getAppDiskUsed();

  /**
   * 包名
   *
   * @return
   */
  String getPackageName();

  /**
   * 应用名称
   *
   * @return
   */
  String getAppName();

  /**
   * 当前的位置
   *
   * @return
   */
  Location getLocation();

  /**
   * Log 数据库的名称
   *
   * @return
   */
  String getDatabaseName();

  /**
   * 当前 App 配置的发送的策略.
   *
   * @return
   */
  LogSender createLogSender(UploadLogPolicyChangedCallback callback);

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
   *
   * Log 的删除机制，是根据过期时间，加上 失败 {@link #getLogRetentionTime()} 次数，
   * 两个都满足的情况下，丢弃 Log
   *
   * @return
   */
  int getMaxFailedCount();

  /**
   * 是否开启 Log，用于验证阶段的测试开关.
   *
   * Log 稳定后，就不需要再开启.
   *
   * @return
   */
  boolean enableLog();

  /**
   * 本地时间和服务器时间的差值
   *
   * @return null 未校时， serverTime - localTime
   */
  Long getDeviceTimeDiff();

}
