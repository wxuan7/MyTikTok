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

public class UploadUploadLogService extends Service implements UploadLogPolicyChangedCallback {

  public static final String KEY_LOG = "log";
  public static final String KEY_PAGE = "keyPage";
  public static final String KEY_DESTROY_PAGE = "destroyCreate";
  public static final String KEY_BEFORE_PAGE_CREATE = "beforePageCreate";
  public static final String KEY_REAL_TIME = "realTime";
  public static final String KEY_START_PAGE = "startPage";
  public static final String KEY_STOP_PAGE = "stopPage";
  private static final String TAG = "log.UploadUploadLogService";
  private static final boolean DEBUG = true;

  private UploadLogStorage mStorage;
  private Handler mWorkHandler;
  private UploadLogHandler mUploadLogHandler;
  
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
      final UploadLogConfiguration configuration = UploadLogManager.CONFIGURATION;
      mWorkHandler.post(new Runnable() {
        @Override
        public void run() {
          mStorage = null;// todo 未来确定了本地数据库之后，需要写一个实现来储存日志
          mUploadLogHandler = null;// todo 未来确定了网络框架之后，需要写一个来上传日志
        }
      });
    }
  }

  private void addLog(final byte[] logContent, final boolean realTime) {
    if (logContent != null) {
      if (realTime && mUploadLogPolicy.getUploadPolicy() != UploadLogPolicy.Upload.NONE) {
        // 如果是实时 Log. 将任务放到队首立马执行
        mWorkHandler.postAtFrontOfQueue(new Runnable() {
          @Override
          public void run() {
            try {
              Object reportEvent = null;// todo 未来确定了日志结构之后，再初始化
        
              // 主动添加一次获取当前 id
              long id = mStorage.addLog(reportEvent);
              // 删除掉当前 id
              mStorage.deleteLog(id);

              Object batchReportEvent = null;// todo 未来确定了日志结构之后，再初始化
              mUploadLogHandler.sendRealLog(batchReportEvent);
            } catch (Exception e) {}
          }
        });
      } else {
        mWorkHandler.post(new Runnable() {
          @Override
          public void run() {
            try {
              Object reportEvent = null;// todo 未来确定了日志结构之后，再初始化
              mStorage.addLog(reportEvent);
            } catch (Exception e) {}
          }
        });
      }
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      return START_STICKY;
    }

    /**
     * 这里是做一层保险，防止 binder 失效的时候，通过 {@link startService() 的方式添加 log}
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
