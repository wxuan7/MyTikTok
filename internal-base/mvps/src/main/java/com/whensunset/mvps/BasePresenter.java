package com.whensunset.mvps;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.View;

import com.whensunset.annotation.inject.Injector;
import com.whensunset.annotation.inject.Injectors;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by whensunset on 2018/7/28.
 */

public class BasePresenter implements Presenter {
  private View mRootView;
  private List<Presenter> mChildPresenterList = new ArrayList<>();
  private boolean isValid = true;
  private boolean isBinding = false;
  private boolean isInitialized = false;
  private Injector mInjector = null;
  
  
  @Override
  public void create(View view) {
    if (isInitialized()) {
      
      throw new IllegalStateException("Presenter只能被初始化一次!");
    }
    
    try {
      mRootView = view;
      
      ButterKnife.bind(this, view);
      
      createChildren();
      
      onCreate();
      
    } catch (Exception e) {
      isValid = false;
      // TODO 创建失败，之后打log或者埋点
    }
    isInitialized = true;
  }
  
  private void createChildren() {
    for (Presenter childPresenter : mChildPresenterList) {
      childPresenter.create(mRootView);
    }
  }
  
  protected void onCreate() {
  
  }
  
  @Override
  public void bind(Object... callerContext) {
    if (!isValid) {
      return;
    }
    
    if (!isInitialized) {
      throw new IllegalStateException("Presenter必须先初始化!");
    }
    
    if (mInjector == null) {
      mInjector = Injectors.injector(getClass());
    }
    
    mInjector.reset(this);
    
    bindChild(callerContext);
    
    if (callerContext != null) {
      for (Object context : callerContext) {
        mInjector.inject(this, context);
      }
    }
    
    onBind(callerContext);
    
    isBinding = true;
  }
  
  private void bindChild(Object... callerContext) {
    for (Presenter childPresenter : mChildPresenterList) {
      childPresenter.bind(callerContext);
    }
  }
  
  protected void onBind(Object... callerContext) {
  
  }
  
  @Override
  public void unbind() {
    if (!isValid) {
      return;
    }
    
    if (!isInitialized) {
      throw new IllegalStateException("Presenter必须先初始化!");
    }
    
    if (!isBinding) {
      throw new IllegalStateException("Presenter 必须处于绑定状态才能解绑!");
    }
    
    onUnbind();
    
    unbindChild();
    
  }
  
  protected void onUnbind() {
  
  }
  
  private void unbindChild() {
    for (Presenter childPresenter : mChildPresenterList) {
      childPresenter.unbind();
    }
  }
  
  @Override
  public void destroy() {
    if (!isValid) {
      return;
    }
    
    if (!isInitialized) {
      throw new IllegalStateException("Presenter必须先初始化!");
    }
    
    onDestroy();
    
    destroyChild();
  }
  
  protected void onDestroy() {
  
  }
  
  private void destroyChild() {
    for (Presenter childPresenter : mChildPresenterList) {
      childPresenter.destroy();
    }
  }
  
  @Override
  public boolean isInitialized() {
    return isInitialized;
  }
  
  public boolean isBinding() {
    return isBinding;
  }
  
  @Override
  public Activity getActivity() {
    
    Activity activity = null;
    if (getContext().getClass().getName().contains("com.android.internal.policy.DecorContext")) {
      try {
        Field field = getContext().getClass().getDeclaredField("mPhoneWindow");
        field.setAccessible(true);
        Object obj = field.get(getContext());
        java.lang.reflect.Method m1 = obj.getClass().getMethod("getContext");
        activity = (Activity) (m1.invoke(obj));
        
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      Context context = getContext();
      while (context instanceof ContextWrapper) {
        if (context instanceof Activity) {
          return (Activity) context;
        }
        context = ((ContextWrapper) context).getBaseContext();
      }
      return (Activity) getContext();
    }
    return activity;
  }
  
  @Override
  public Presenter add(Presenter presenter) {
    if (presenter == null) {
      return this;
    }
    mChildPresenterList.add(presenter);
    
    if (isInitialized()) {
      presenter.create(mRootView);
    }
    return this;
  }
  
  public View getRootView() {
    return mRootView;
  }
  
  protected final Context getContext() {
    return mRootView == null ? null : mRootView.getContext();
  }
  
  protected final Resources getResources() {
    if (getContext() == null) {
      return null;
    }
    return getContext().getResources();
  }
  
  protected final String getString(int id) {
    if (getContext() == null) {
      return null;
    }
    return getContext().getString(id);
  }
  
}
