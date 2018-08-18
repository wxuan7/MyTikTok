package com.example.whensunset.mytiktok;

import com.whensunset.annotation.inject.Inject;
import com.whensunset.mvps.BasePresenter;

/**
 * Created by whensunset on 2018/8/6.
 */

public class TestPresenter extends BasePresenter {
  
  @Inject("mTextString")
  String mTextString;
  
  @Override
  protected void onBind(Object... callerContext) {
    super.onBind(callerContext);
    System.out.println("aa");
  }
}
