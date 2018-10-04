package com.whensunset.annotation.field;

import com.whensunset.annotation.invoker.ForInvoker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FieldGetters {
  public static final String INVOKER_ID = "Injectors";
  
  private static final FieldGetter NOOP = new FieldGetter() {
    @Override
    public FieldGetter init() {
      return this;
    }
    
    @Override
    public Object get(Object target, String field) {
      return null;
    }
    
    @Override
    public void set(Object target, String field, Object value) {
    
    }
    
    @Override
    public Set<Object> allFields(Object target) {
      return Collections.emptySet();
    }
    
    @Override
    public Set<String> allFieldNames(Object target) {
      return Collections.emptySet();
    }
    
    @Override
    public Set<Class> allTypes(Object target) {
      return Collections.emptySet();
    }
  };
  
  private static final Map<Class, FieldGetter> FIELD_GETTER_MAP = new HashMap<>();
  
  public static void putAll(Map<Class, FieldGetter> map) {
    FIELD_GETTER_MAP.putAll(map);
  }
  
  public static void put(Class clazz, FieldGetter fieldGetter) {
    FIELD_GETTER_MAP.put(clazz, fieldGetter);
  }
  
  public static FieldGetter getFieldGetter(Class clazz) {
    FieldGetter fieldGetter = FIELD_GETTER_MAP.get(clazz);
    if (fieldGetter == null) {
      fieldGetter = findSuperFieldGetter(clazz);
      if (fieldGetter != null) {
        FIELD_GETTER_MAP.put(clazz, fieldGetter);
      }
    }
    return fieldGetter == null ? null : fieldGetter.init();
  }
  
  public static FieldGetter fieldGetterOrNoop(Class clazz) {
    FieldGetter fieldGetter = getFieldGetter(clazz);
    return (fieldGetter == null ? NOOP : fieldGetter);
  }
  
  public static FieldGetter findSuperFieldGetter(Class clazz) {
    clazz = clazz.getSuperclass();
    while (clazz != null) {
      FieldGetter fieldGetter = FIELD_GETTER_MAP.get(clazz);
      if (fieldGetter != null) {
        return fieldGetter.init();
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }
  
  public static FieldGetter superFieldGetterOrNoop(Class clazz) {
    FieldGetter fieldGetter = findSuperFieldGetter(clazz);
    return (fieldGetter == null ? NOOP : fieldGetter);
  }
  
  @ForInvoker(methodId = INVOKER_ID)
  public static void init() {
  }
}
