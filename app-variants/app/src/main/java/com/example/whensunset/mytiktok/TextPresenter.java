package com.example.whensunset.mytiktok;

import android.content.Intent;
import android.widget.TextView;

import com.whensunset.annotation.inject.Inject;
import com.whensunset.mvps.BasePresenter;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by whensunset on 2018/8/19.
 */

public class TextPresenter extends BasePresenter {
  
  @BindView(R.id.text)
  TextView mTextView;
  
  @Inject("mTextString")
  String mText;
  
  @Override
  public void bind(Object... callerContext) {
    super.bind(callerContext);
    mTextView.setText(mText);
  }
  
  @OnClick(R.id.text)
  void onClick() {
    getActivity().startActivity(new Intent(getContext(), TestActivity.class));
  }
}
