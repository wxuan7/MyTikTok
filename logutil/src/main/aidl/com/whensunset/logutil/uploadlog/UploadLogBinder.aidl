// UploadLogBinder.aidl
package com.whensunset.logutil.uploadlog;

// Declare any non-default types here with import statements

interface UploadLogBinder {
   // 记录 log.
   void log(boolean realTime, in byte[] content);
}
