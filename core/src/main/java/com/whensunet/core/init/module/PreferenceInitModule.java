package com.whensunet.core.init.module;

import android.content.Context;
import android.content.SharedPreferences;

import com.whensunet.core.init.InitModule;
import com.whensunet.core.util.PreferenceUtil;
import com.whensunset.annotation.preference.PreferenceAdapter;
import com.whensunset.annotation.preference.PreferenceContext;
import com.whensunset.http.retrofit.utils.Gsons;

import java.lang.reflect.Type;

public class PreferenceInitModule extends InitModule {
  @Override
  public void onApplicationAttachBaseContext(Context base) {
    super.onApplicationAttachBaseContext(base);
    PreferenceContext.init(new PreferenceAdapter() {
      private SharedPreferences mPreference;
      
      @Override
      public SharedPreferences getPreferenceByName(String key) {
        if (mPreference == null) {
          mPreference = PreferenceUtil.getPreferences();
        }
        if ("DefaultPreferenceHelper".equals(key)) {
          return mPreference;
        }
        return null;
      }
      
      @Override
      public String getPreferenceKeyPrefix(String key) {
        return "";
      }
      
      @Override
      public <D> D deserialize(String json, Type type) {
        try {
          return Gsons.GSON.fromJson(json, type);
        } catch (Throwable t) {
        }
        return null;
      }
      
      @Override
      public String serialize(Object obj) {
        if (obj == null) {
          return null;
        }
        try {
          return Gsons.GSON.toJson(obj);
        } catch (Throwable t) {
        }
        return "";
      }
    });
  }
}
