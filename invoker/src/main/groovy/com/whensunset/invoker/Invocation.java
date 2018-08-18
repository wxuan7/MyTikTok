package com.whensunset.invoker;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Invocation implements Serializable {
  private static final long serialVersionUID = -4920559937353697607L;
  @SerializedName("target")
  public InvokeMethod mTarget;
  @SerializedName("invoker")
  public InvokeMethod mInvoker;

  @Override
  public boolean equals(Object o) {
    return o instanceof Invocation && mTarget.equals(((Invocation) o).mTarget)
        && mInvoker.equals(((Invocation) o).mInvoker);
  }

  @Override
  public int hashCode() {
    return mInvoker.hashCode();
  }
}
