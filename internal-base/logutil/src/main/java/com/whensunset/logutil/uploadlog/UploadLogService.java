package com.whensunset.logutil.uploadlog;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.whensunset.logutil.uploadlog.config.UploadLogConfiguration;
import com.whensunset.logutil.uploadlog.policy.UploadLogPolicy;
import com.whensunset.logutil.uploadlog.policy.UploadLogPolicyChangedCallback;
import com.whensunset.logutil.uploadlog.sender.UploadLogHandler;
import com.whensunset.logutil.uploadlog.storage.UploadLogStorage;

public class UploadLogService extends Service implements UploadLogPolicyChangedCallback {
  
  public static final String KEY_LOG = "log";
  public static final String KEY_REAL_TIME = "realTime";
  private static final String TAG = "@UploadLogService";
  
  private UploadLogStorage mStorage;
  private Handler mWorkHandler;
  private UploadLogHandler mUploadLogHandler;
  private UploadLogConfiguration mUploadLogConfiguration;
  private volatile UploadLogPolicy mUploadLogPolicy = UploadLogPolicy.DEFAULT;
  
  private UploadLogBinder.Stub mStub = new UploadLogBinder.Stub() {
    @Override
    /**
     * realTime 是否是实时 log. 实时 log,如果LogPolicy允许则马上发送.
     */
    public void log(boolean realTime, final byte[] content) throws RemoteException {
      addLog(content, realTime);
    }
  };
  
  @Override
  public void onCreate() {
    super.onCreate();
    
    HandlerThread thread = new HandlerThread("log-manager", Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    if (mWorkHandler == null) {
      mWorkHandler = new Handler(thread.getLooper());
      mUploadLogConfiguration = UploadLogManager.CONFIGURATION;
      mWorkHandler.post(() -> {
        mStorage = null;// todo 未来确定了本地数据库之后，需要写一个实现来储存日志
        mUploadLogHandler = null;// todo 未来确定了网络框架之后，需要写一个来上传日志
      });
    }
  }
  
  private void addLog(final byte[] logContent, final boolean realTime) {
    if (logContent == null) {
      return;
    }
    if (realTime && mUploadLogPolicy.getUploadPolicy() != UploadLogPolicy.Upload.NONE) {
      // 如果是实时 Log. 将任务放到队首立马执行
      mWorkHandler.postAtFrontOfQueue(() -> {
        try {
          Object reportEvent = new Object();// todo 未来确定了日志结构之后，再初始化
          
          // 主动添加一次获取当前 id
          long id = mStorage.addLog(reportEvent);
          // 删除掉当前 id
          mStorage.deleteLog(id);
          
          Object[] logs = {reportEvent};
          mUploadLogHandler.sendRealLog(logs);
        } catch (Exception ignored) {
          // 日志不能影响 app 的运行，所以忽略日志发生的异常
        }
      });
    } else {
      mWorkHandler.post(() -> {
        try {
          Object reportEvent = new Object();// todo 未来确定了日志结构之后，再初始化
          mStorage.addLog(reportEvent);
        } catch (Exception ignored) {
          // 日志不能影响 app 的运行，所以忽略日志发生的异常
        }
      });
    }
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      return START_STICKY;
    }
    
    /**
     * 这里是做一层保险，在binder 失效的时候，通过 {@link startService() 的方式添加 log}
     */
    addLog(intent.getByteArrayExtra(KEY_LOG), intent.getBooleanExtra(KEY_REAL_TIME, false));
    
    return START_STICKY;
  }
  
  @Override
  public IBinder onBind(Intent intent) {
    return mStub;
  }
  
  @Override
  public void onLogPolicyChanged(UploadLogPolicy policy) {
    if (mUploadLogPolicy == policy) {
      return;
    }
    if (policy.getSavePolicy() != mUploadLogPolicy.getSavePolicy()) {
      mStorage.setSavePolicy(policy.getSavePolicy());
    }
    if (mUploadLogPolicy.getUploadPolicy() != policy.getUploadPolicy()) {
      mUploadLogHandler.setUploadPolicy(policy.getUploadPolicy());
    }
    mUploadLogPolicy = policy;
  }
  
  @Override
  public void onSendIntervalChanged(long interval) {
    mUploadLogHandler.setUploadIntervalMs(interval);
  }
}
