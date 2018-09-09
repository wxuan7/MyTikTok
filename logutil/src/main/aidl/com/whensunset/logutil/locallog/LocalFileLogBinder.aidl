// LocalFileLogBinder.aidl
package com.whensunset.logutil.locallog;

// Declare any non-default types here with import statements

interface LocalFileLogBinder {
   // 记录 log.
   void log(String tag, String message, String context);
}
