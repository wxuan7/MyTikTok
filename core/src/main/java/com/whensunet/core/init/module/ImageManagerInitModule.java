package com.whensunet.core.init.module;

import android.app.Application;

import com.whensunet.core.init.InitModule;
import com.whensunset.image.ImageManager;

public class ImageManagerInitModule extends InitModule {
  
  @Override
  public void onApplicationCreate(Application application) {
    ImageManager.initialize(application);
  }
  
}
