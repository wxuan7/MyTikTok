package com.whensunset.annotation.inject;

import com.whensunset.annotation.field.FieldGetters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FieldProviderImpl extends FieldProvider {
  @Override
  public <F, T> T get(F obj, String fieldName) {
    if (obj == null) {
      return null;
    }
    T result = null;
    if (obj instanceof NamedParam && fieldName.equals(((NamedParam) obj).mName)) {
      return (T) ((NamedParam) obj).mParam;
    }
    if (obj instanceof Map && ((Map) obj).containsKey(fieldName)) {
      result = (T) ((Map) obj).get(fieldName);
    }
    if (result != null) {
      return result;
    }
    result = (T) FieldGetters.fieldGetterOrNoop(obj.getClass()).get(obj, fieldName);
    if (result != null) {
      return result;
    }
    return null;
  }
  
  @Override
  public <F, T> void set(F obj, String fieldName, T value) {
    if (obj == null) {
      return;
    }
    FieldGetters.fieldGetterOrNoop(obj.getClass()).set(obj, fieldName, value);
  }
  
  @Override
  public Set<String> allFieldNames(Object obj) {
    if (obj == null) {
      return Collections.emptySet();
    }
    if (obj instanceof NamedParam) {
      return Collections.singleton(((NamedParam) obj).mName);
    }
    if (obj instanceof Map) {
      return ((Map) obj).keySet();
    }
    return FieldGetters.fieldGetterOrNoop(obj.getClass()).allFieldNames(obj);
  }
  
  @Override
  public Set<Class> allTypes(Object obj) {
    if (obj == null) {
      return Collections.emptySet();
    }
    final Set<Class> result = new HashSet<>();
    result.addAll(FieldGetters.fieldGetterOrNoop(obj.getClass()).allTypes(obj));
    result.add(obj.getClass());
    return result;
  }
  
  @Override
  public Set<Object> allDirectFields(Object obj) {
    if (obj == null) {
      return Collections.emptySet();
    }
    return FieldGetters.fieldGetterOrNoop(obj.getClass()).allFields(obj);
  }
}
