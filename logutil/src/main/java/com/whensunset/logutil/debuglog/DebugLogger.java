package com.whensunset.logutil.debuglog;

import android.text.TextUtils;

public final class DebugLogger {

  private static Logger sLogger = Logger.DEFAULT;

  public interface Logger {
    void log(LEVEL level, String tag, String message, Throwable tr);

    /** A {@link Logger} defaults output appropriate for the current platform. */
    Logger DEFAULT = new Logger() {
      @Override
      public void log(LEVEL level, String tag, String message, Throwable tr) {
        log2Console(level, tag, message, tr);
      }
    };
  }

  public static void setLogger(Logger logger) {
    DebugLogger.sLogger = logger;
  }

  /**
   * Whether to enable the log
   */
  private static boolean sIsEnabled = true;

  private DebugLogger() {}

  private static void log(LEVEL level, String tag, String msg, Throwable tr) {
    if (!sIsEnabled) {
      return;
    }
    sLogger.log(level, tag, msg, tr);
  }

  /**
   * Get the final tag from the tag.
   */
  private static String getCurrentTag(String tag) {
    if (!TextUtils.isEmpty(tag)) {
      return tag;
    }

    StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
    if (stacks.length >= 4) {
      return stacks[3].getClassName();
    }

    return null;
  }

  /**
   * write the log messages to the console.
   */
  private static void log2Console(LEVEL level, String tag, String msg, Throwable thr) {
    switch (level) {
      case VERBOSE:
        if (thr == null) {
          android.util.Log.v(tag, msg);
        } else {
          android.util.Log.v(tag, msg, thr);
        }
        break;
      case DEBUG:
        if (thr == null) {
          android.util.Log.d(tag, msg);
        } else {
          android.util.Log.d(tag, msg, thr);
        }
        break;
      case INFO:
        if (thr == null) {
          android.util.Log.i(tag, msg);
        } else {
          android.util.Log.i(tag, msg, thr);
        }
        break;
      case WARN:
        if (thr == null) {
          android.util.Log.w(tag, msg);
        } else if (TextUtils.isEmpty(msg)) {
          android.util.Log.w(tag, thr);
        } else {
          android.util.Log.w(tag, msg, thr);
        }
        break;
      case ERROR:
        if (thr == null) {
          android.util.Log.e(tag, msg);
        } else {
          android.util.Log.e(tag, msg, thr);
        }
        break;
      case ASSERT:
        if (thr == null) {
          android.util.Log.wtf(tag, msg);
        } else if (TextUtils.isEmpty(msg)) {
          android.util.Log.wtf(tag, thr);
        } else {
          android.util.Log.wtf(tag, msg, thr);
        }
        break;
      default:
        break;
    }
  }

  /**
   * enable or disable the log, the default value is true.
   *
   * @param enabled whether to enable the log
   */
  public static void setEnabled(boolean enabled) {
    sIsEnabled = enabled;
  }

  /**
   * Checks to see whether or not a log for the specified tag is loggable at the specified level.
   * The default level of
   * any tag is set to INFO. This means that any level above and including INFO will be logged.
   * Before you make any
   * calls to a logging method you should check to see if your tag should be logged.
   *
   * @param tag The tag to check
   * @param level The level to check
   * @return Whether or not that this is allowed to be logged.
   */
  public static boolean isLoggable(String tag, int level) {
    return android.util.Log.isLoggable(tag, level);
  }

  /**
   * Send a VERBOSE log message.
   *
   * @param msg The message you would like logged.
   */
  public static void v(String tag, String msg) {
    log(LEVEL.VERBOSE, tag, msg, null);
  }

  /**
   * Send a VERBOSE log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void v(String tag, String msg, Throwable thr) {
    log(LEVEL.VERBOSE, tag, msg, thr);
  }

  /**
   * Send a DEBUG log message.
   *
   * @param msg The message you would like logged.
   */
  public static void d(String tag, String msg) {
    log(LEVEL.DEBUG, tag, msg, null);
  }

  /**
   * Send a DEBUG log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void d(String tag, String msg, Throwable thr) {
    log(LEVEL.DEBUG, tag, msg, thr);
  }

  /**
   * Send a INFO log message.
   *
   * @param msg The message you would like logged.
   */
  public static void i(String tag, String msg) {
    log(LEVEL.INFO, tag, msg, null);
  }

  /**
   * Send a INFO log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void i(String tag, String msg, Throwable thr) {
    log(LEVEL.INFO, tag, msg, thr);
  }

  /**
   * Send a WARN log message.
   *
   * @param msg The message you would like logged.
   */
  public static void w(String tag, String msg) {
    log(LEVEL.WARN, tag, msg, null);
  }

  /**
   * Send a WARN log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void w(String tag, String msg, Throwable thr) {
    log(LEVEL.WARN, tag, msg, thr);
  }

  /**
   * Send a WARN log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void w(String msg, Throwable thr) {
    log(LEVEL.WARN, null, msg, thr);
  }

  /**
   * Send a ERROR log message.
   *
   * @param msg The message you would like logged.
   */
  public static void e(String tag, String msg) {
    log(LEVEL.ERROR, tag, msg, null);
  }

  /**
   * Send a ERROR log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void e(String tag, String msg, Throwable thr) {
    log(LEVEL.ERROR, tag, msg, thr);
  }

  public static void catchedException(String tag, Throwable thr) {
    log(LEVEL.ERROR, tag, thr.getMessage(), thr);
  }

  /**
   * Send a What a Terrible Failure log message.
   *
   * @param msg The message you would like logged.
   */
  public static void wtf(String tag, String msg) {
    log(LEVEL.ASSERT, tag, msg, null);
  }

  /**
   * Send a What a Terrible Failure log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void wtf(String tag, String msg, Throwable thr) {
    log(LEVEL.ASSERT, tag, msg, thr);
  }

  /**
   * Send a What a Terrible Failure log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public static void wtf(String msg, Throwable thr) {
    log(LEVEL.ASSERT, null, msg, thr);
  }

  public enum LEVEL {
    VERBOSE(2, "V"), DEBUG(3, "D"), INFO(4, "I"), WARN(5, "W"), ERROR(6, "E"), ASSERT(7, "A");

    final String levelString;
    final int level;

    private LEVEL(int level, String levelString) {
      this.level = level;
      this.levelString = levelString;
    }

    public String getLevelString() {
      return this.levelString;
    }

    public int getLevel() {
      return this.level;
    }
  }
}
