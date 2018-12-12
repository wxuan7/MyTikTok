package com.whensunset.invoker.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class InvokeMethod implements Serializable {
  private static final long serialVersionUID = 3213094601630741209L;
  @SerializedName("class")
  public String className;
  @SerializedName("method")
  public String methodName;
  
  @Override
  public boolean equals(Object o) {
    return o instanceof InvokeMethod && className.equals(((InvokeMethod) o).className)
        && methodName.equals(((InvokeMethod) o).methodName);
  }
  
  @Override
  public int hashCode() {
    return className.hashCode();
  }
}
