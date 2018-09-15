package com.example.whensunset.mytiktok;

import android.app.Application;

import com.whensunset.annotation.field.FieldGetters;
import com.whensunset.annotation.inject.FieldProviderImpl;
import com.whensunset.annotation.inject.Injectors;
import com.whensunset.annotation.inject.ProviderHolder;

/**
 * Created by whensunset on 2018/8/19.
 */

public class MytiktokApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Injectors.init();
    FieldGetters.init();
    ProviderHolder.setProvider(new FieldProviderImpl());
  }
}
