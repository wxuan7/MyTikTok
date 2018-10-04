package com.whensunet.core.factoryregister;

import com.whensunset.annotation.invoker.ForInvoker;
import com.whensunset.annotation.singleton.Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by whensunset on 2018/10/2.
 */

public class RegisterCall {
  public static final String INVOKER_ID = "REGISTER_CALL_INVOKER_ID";
  private static Map<Class, List<RegisterFactoryMapping>> REGISTER_FACTORY_MAP = new HashMap<>();
  
  public static Map<Class, List<RegisterFactoryMapping>> getConfig() {
    doRegister();
    return REGISTER_FACTORY_MAP;
  }
  
  @ForInvoker(methodId = INVOKER_ID)
  public static void doRegister() {
    REGISTER_FACTORY_MAP = null;
  }
  
  public static <T> void register(Class<T> implementClass, Factory<? extends T> factory,
                                  int minSdk) {
    RegisterFactoryMapping mapping = new RegisterFactoryMapping(implementClass, factory, minSdk);
    List<RegisterFactoryMapping> mappingList = REGISTER_FACTORY_MAP.get(mapping.mImplementClass);
    if (mappingList == null) {
      mappingList = new ArrayList<>();
      REGISTER_FACTORY_MAP.put(mapping.mImplementClass, mappingList);
    }
    mappingList.add(mapping);
  }
  
  static class RegisterFactoryMapping {
    public final Class<?> mImplementClass;
    public final Factory<?> mFactory;
    public final int mMinSdk;

    public RegisterFactoryMapping(Class<?> implementClass, Factory<?> factory,
                                  int sdk) {
      mImplementClass = implementClass;
      mFactory = factory;
      mMinSdk = sdk;
    }
  }
}
