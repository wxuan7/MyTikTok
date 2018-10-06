package com.whensunset.annotation.inject;

import java.util.Set;

public abstract class FieldProvider {
  public abstract <F, T> T get(F obj, String fieldName);
  
  public abstract <F, T> void set(F obj, String fieldName, T value);
  
  public abstract Set<String> allFieldNames(Object obj);
  
  public abstract Set<Class> allTypes(Object obj);
  
  public final <F> boolean have(F obj, String fieldName) {
    Set<String> names = allFieldNames(obj);
    return names != null && names.contains(fieldName);
  }
  
  public abstract Set<Object> allDirectFields(Object obj);
}
