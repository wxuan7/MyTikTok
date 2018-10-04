package com.whensunet.core.retrofit;

import com.whensunet.core.retrofit.service.CaptureApiService;
import com.whensunset.annotation.singleton.Factory;
import com.whensunset.http.retrofit.RetrofitFactory;
import com.whensunset.http.retrofit.utils.RetrofitSchedulers;

/**
 * Created by whensunset on 2018/10/4.
 */

public class CaptureApiServiceFactory implements Factory<CaptureApiService>{
  
  @Override
  public CaptureApiService newInstance() {
    return RetrofitFactory
        .newBuilder(new DefualtRetrofitConfig(RetrofitSchedulers.NETWORKING))
        .build().create(CaptureApiService.class);
  }
}
