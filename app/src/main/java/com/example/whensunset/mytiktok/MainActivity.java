package com.example.whensunset.mytiktok;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.whensunset.annotation.field.Field;
import com.whensunset.mvps.Presenter;

public class MainActivity extends AppCompatActivity {
  
  @Field("mTextString")
  String mTextString = "mTextString";
  
  @Field("mImage")
  Drawable mImage;
  
  Presenter mPresenter;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  
    mPresenter = new LinearPresenter();
    mPresenter.create(getWindow().getDecorView());
    
    mImage = getDrawable(R.drawable.image);
    
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    mPresenter.bind(this);
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    mPresenter.unbind();
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
    mPresenter.destroy();
  }
}
