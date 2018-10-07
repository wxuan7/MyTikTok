package com.whensunet.core.retrofit;

import com.whensunet.core.retrofit.service.CaptureApiService;
import com.whensunset.annotation.singleton.Factory;
import com.whensunset.http.RetrofitFactory;
import com.whensunset.http.utils.RetrofitSchedulers;

/**
 * Created by whensunset on 2018/10/4.
 */

public class CaptureApiServiceFactory implements Factory<CaptureApiService> {
  
  @Override
  public CaptureApiService newInstance() {
    return RetrofitFactory
        .newBuilder(new DefaultRetrofitConfig(RetrofitSchedulers.NETWORKING))
        .build().create(CaptureApiService.class);
  }
}
