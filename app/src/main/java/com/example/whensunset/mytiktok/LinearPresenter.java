package com.example.whensunset.mytiktok;

import com.whensunset.mvps.BasePresenter;

/**
 * Created by whensunset on 2018/8/19.
 */

public class LinearPresenter extends BasePresenter {
  
  public LinearPresenter() {
    add(new TextPresenter());
    add(new ImagePresenter());
  }
}
