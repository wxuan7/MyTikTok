package com.whensunset.annotation.inject;

import com.whensunset.annotation.invoker.ForInvoker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class Injectors {
  public static final String INVOKER_ID = "Injectors";
  private static final Injector NOOP = new Injector() {
    @Override
    public void inject(Object target, Object accessible) {}

    @Override
    public Set<String> allNames() {
      return Collections.emptySet();
    }

    @Override
    public Set<Class> allTypes() {
      return Collections.emptySet();
    }

    @Override
    public void reset(Object target) {

    }
  };
  private static final Map<Class, Injector> INJECTOR_MAP = new HashMap<>();

  public static void putAll(Map<Class, Injector> map) {
    INJECTOR_MAP.putAll(map);
  }

  public static void put(Class clazz, Injector injector) {
    INJECTOR_MAP.put(clazz, injector);
  }

  @Nonnull
  public static Injector injector(Class clazz) {
    Injector injector = INJECTOR_MAP.get(clazz);
    return (injector == null ? NOOP : injector);
  }

  @ForInvoker(methodId = INVOKER_ID)
  public static void init(){
  }
}
