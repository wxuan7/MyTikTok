package com.example.whensunset.mytiktok;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.whensunset.annotation.inject.Inject;
import com.whensunset.mvps.BasePresenter;

import butterknife.BindView;

/**
 * Created by whensunset on 2018/8/19.
 */

public class ImagePresenter extends BasePresenter {
  @BindView(R.id.image)
  ImageView mImageView;
  
  @Inject("mImage")
  Drawable mImage;
  
  private Bitmap mBitmap;
  private boolean isFirstBind = true;
  
  // 整个生命周期中只允许运行一次
  @Override
  public void create(View view) {
    super.create(view);
    mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_white);
  }
  
  // 整个生命周期允许运行多次，与 unbind 成对调用
  @Override
  public void bind(Object... callerContext) {
    super.bind(callerContext);
    if (isFirstBind) {
      mImageView.setImageDrawable(mImage);
      isFirstBind = false;
    } else {
      mImageView.setImageDrawable(new BitmapDrawable(mBitmap));
    }
  }
  
  // 整个生命周期允许运行多次，与 bind 成对调用
  @Override
  public void unbind() {
    super.unbind();
    mImage = null;
  }
  
  // 整个生命周期只允许调用一次，与 create 成对调用
  @Override
  public void destroy() {
    super.destroy();
    mBitmap.recycle();
    mBitmap = null;
  }
}
