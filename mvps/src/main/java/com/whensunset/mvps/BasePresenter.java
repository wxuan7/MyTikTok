package com.whensunset.mvps;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import com.whensunset.annotation.inject.Injector;
import com.whensunset.annotation.inject.Injectors;
import com.whensunset.annotation.inject.ProviderHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  public void init(View view) {
    if (isInitialized()) {
      
      throw new IllegalStateException("Presenter只能被初始化一次!");
    }
  
    try {
      mRootView = view;
      
      ButterKnife.bind(this, view);
  
      initChildren();
      
      onInit();
    
    } catch (Exception e) {
      isValid = false;
      // TODO 创建失败，之后打log或者埋点
    }
    isInitialized = true;
  }
  
  private void initChildren() {
    for (Presenter childPresenter: mChildPresenterList) {
      childPresenter.init(mRootView);
    }
  }
  
  protected void onInit() {
  
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
    
    checkInjection(callerContext);
  
    mInjector.reset(this);
  
    bindChild(callerContext);
  
    if (callerContext != null) {
      for (Object context : callerContext) {
        mInjector.inject(this, context);
      }
    }
    
    onBind(callerContext);
    
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
  
  @Override
  public Activity getActivity() {
    Context context = getContext();
    while (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        return (Activity) context;
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    return (Activity) getContext();
  }
  
  @Override
  public Presenter add(Presenter presenter) {
    if (presenter == null) {
      return this;
    }
    mChildPresenterList.add(presenter);
    
    if (isInitialized()) {
      presenter.init(mRootView);
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
  
  /*
   * 用于在开发的时候检查在注入的时候是否有问题,未来比如真正上线的时候会干掉
   */
  private void checkInjection(Object... callerContexts) {
    Set<Class> allTypes = new HashSet<>();
    Set<String> allNames = new HashSet<>();
    // for debugging provider origination
    Map<Class, Object> typeOriginMap = new HashMap<>();
    Map<String, Object> nameOriginMap = new HashMap<>();
    for (Object callerContext : callerContexts) {
      Collection<String> names = null;
      Collection<Class> types = null;
      if (callerContext instanceof Map) {
        names = ((Map) callerContext).keySet();
      } else {
        names = ProviderHolder.allFieldNames(callerContext);
        types = ProviderHolder.allTypes(callerContext);
      }
      
      if (types != null) {
        for (Class type : types) {
          if (!allTypes.add(type)) {
            throw new IllegalArgumentException("Field 类型冲突，class " + type.getCanonicalName());
          }
          typeOriginMap.put(type, callerContext);
        }
      }
      
      for (String name : names) {
        if (!allNames.add(name)) {
          throw new IllegalArgumentException("Field key冲突，key " + name);
        }
        nameOriginMap.put(name, callerContext);
      }
    }
    
    if (mInjector == null) {
      return;
    }
    Set<Class> typesNeeded = mInjector.allTypes();
    Set<String> namesNeeded = mInjector.allNames();
    
    if (!allTypes.containsAll(typesNeeded)) {
      Set<Class> missingTypes = new HashSet<>(typesNeeded);
      missingTypes.removeAll(allTypes);
      throw new IllegalArgumentException(this.getClass().getSimpleName() +
          " Inject 类型缺失，类型 " + missingTypes + " in " + getClass().getSimpleName());
    }
    
    if (!allNames.containsAll(namesNeeded)) {
      Set<String> missingNames = new HashSet<>(namesNeeded);
      missingNames.removeAll(allNames);
      throw new IllegalArgumentException(
          this.getClass().getSimpleName() + " Inject key缺失，keys " + missingNames);
    }
    
    String tag = getClass().getSimpleName();
    for (Class type : typesNeeded) {
      Object origin = typeOriginMap.get(type);
      Log.d(tag, "getByType " + type.getName() + " from " + origin.getClass().getName() + "@"
          + System.identityHashCode(origin));
    }
    for (String name : namesNeeded) {
      Object origin = nameOriginMap.get(name);
      Log.d(tag, "getByName " + name + " from " + origin.getClass().getName() + "@"
          + System.identityHashCode(origin));
    }
  }
}
