package com.whensunet.core.retrofit;

import com.whensunet.core.factoryregister.SingletonCall;
import com.whensunet.core.retrofit.service.CaptureApiService;
import com.whensunet.core.retrofit.service.UserApiService;
import com.whensunset.annotation.invoker.InvokeBy;
import com.whensunset.http.retrofit.RetrofitFactory;
import com.whensunset.http.retrofit.utils.RetrofitSchedulers;

/**
 * Created by whensunset on 2018/10/4.
 */

public class RetrofitServiceCreateCall {
  @InvokeBy(invokerClass = SingletonCall.class, methodId = SingletonCall.INVOKER_ID)
  public static void initRetrofitService() {
    // 可以手写 factory ，也可以 用匿名的内部类
    SingletonCall.register(CaptureApiService.class, new CaptureApiServiceFactory());

    SingletonCall.register(UserApiService.class, () -> RetrofitFactory
        .newBuilder(new DefualtRetrofitConfig(RetrofitSchedulers.NETWORKING))
        .build()
        .create(UserApiService.class));
  }
}
