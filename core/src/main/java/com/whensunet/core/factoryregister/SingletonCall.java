package com.whensunet.core.factoryregister;

import com.whensunset.annotation.invoker.ForInvoker;
import com.whensunset.annotation.invoker.InvokeBy;
import com.whensunset.annotation.singleton.Factory;

/**
 * Created by whensunset on 2018/10/3.
 */

public class SingletonCall {
  public static final String INVOKER_ID = "SINGLETON_CALL";
  public static <T> void register(Class<T> implementClass, Factory<? extends T> factory ) {
    RegisterCall.register(implementClass, factory, 1);
  }

  @ForInvoker(methodId = INVOKER_ID)
  @InvokeBy(invokerClass = RegisterCall.class, methodId = RegisterCall.INVOKER_ID)
  public static void doRegister() {

  }
}
