package com.whensunset.http.retrofit.multipart;

public interface OnProgressListener {
  /**
   * Invoked when progress has been changed
   *
   * @param current Current finished progress
   * @param total   Total progress, NOTE total may be 0 if total is not available
   * @param sender  Sender
   * @return Returns true to terminate processing, false otherwise
   */
  boolean onProgress(int current, int total, Object sender);
}
