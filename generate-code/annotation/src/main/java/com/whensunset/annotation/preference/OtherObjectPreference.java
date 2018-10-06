package com.whensunset.annotation.preference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 用于注释非内置对象
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface OtherObjectPreference {
  String prefFile() default "";
  
  String key() default "";
  
  String prefixKey() default "";
  
  boolean removable() default false;
  
  String def() default "";
}
