package com.whensunset.annotation.field;

import java.util.Set;

public interface FieldGetter<I> {
  FieldGetter<I> init();
  
  <T> T get(I target, String field);
  
  <T> void set(I target, String field, T value);
  
  Set<Object> allFields(I target);
  
  Set<String> allFieldNames(I target);
  
  Set<Class> allTypes(I target);
}
