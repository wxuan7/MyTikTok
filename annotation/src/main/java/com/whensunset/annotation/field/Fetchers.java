package com.whensunset.annotation.field;

import com.whensunset.annotation.invoker.ForInvoker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class Fetchers {
  public static final String INVOKER_ID = "Injectors";

  private static final Fetcher NOOP = new Fetcher() {
    @Override
    public Fetcher init() {
      return this;
    }

    @Override
    public Object get(Object target, Class tClass) {
      return null;
    }

    @Override
    public Object get(Object target, String field) {
      return null;
    }

    @Override
    public void set(Object target, String field, Object value) {

    }

    @Override
    public void set(Object target, Class aClass, Object value) {

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
  
  private static final Map<Class, Fetcher> FETCHER_MAP = new HashMap<>();

  public static void putAll(Map<Class, Fetcher> map) {
    FETCHER_MAP.putAll(map);
  }

  public static void put(Class clazz, Fetcher fetcher) {
    FETCHER_MAP.put(clazz, fetcher);
  }

  public static Fetcher fetcher(Class clazz) {
    Fetcher fetcher = FETCHER_MAP.get(clazz);
    if (fetcher == null) {
      fetcher = findSuperFetcher(clazz);
      if (fetcher != null) {
        FETCHER_MAP.put(clazz, fetcher);
      }
    }
    return fetcher == null ? null : fetcher.init();
  }

  @Nonnull
  public static Fetcher fetcherNonNull(Class clazz) {
    Fetcher fetcher = fetcher(clazz);
    return (fetcher == null ? NOOP : fetcher);
  }

  public static Fetcher findSuperFetcher(Class clazz) {
    clazz = clazz.getSuperclass();
    while (clazz != null) {
      Fetcher fetcher = FETCHER_MAP.get(clazz);
      if (fetcher != null) {
        return fetcher.init();
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }

  @Nonnull
  public static Fetcher superFetcherNonNull(Class clazz) {
    Fetcher fetcher = findSuperFetcher(clazz);
    return (fetcher == null ? NOOP : fetcher);
  }

  @ForInvoker(methodId = INVOKER_ID)
  public static void init(){
  }
}
