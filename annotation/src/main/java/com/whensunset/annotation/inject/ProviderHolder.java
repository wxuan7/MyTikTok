package com.whensunset.annotation.inject;

import java.util.Set;

public final class ProviderHolder {
  private static FieldProvider FIELD_PROVIDER;
  
  public static void setProvider(FieldProvider fieldProvider) {
    FIELD_PROVIDER = fieldProvider;
  }
  
  public static <F, T> T get(F target, String fieldName) {
    return FIELD_PROVIDER.get(target, fieldName);
  }
  
  public static <F> boolean have(F obj, String fieldName) {
    return FIELD_PROVIDER.have(obj, fieldName);
  }
  
  public static <F, T> void set(F obj, String fieldName, T value) {
    FIELD_PROVIDER.set(obj, fieldName, value);
  }
  
  public static Set<String> allFieldNames(Object obj) {
    return FIELD_PROVIDER.allFieldNames(obj);
  }
  
  public static Set<Class> allTypes(Object obj) {
    return FIELD_PROVIDER.allTypes(obj);
  }
  
  public static Set<Object> allFields(Object obj) {
    return FIELD_PROVIDER.allDirectFields(obj);
  }
}
