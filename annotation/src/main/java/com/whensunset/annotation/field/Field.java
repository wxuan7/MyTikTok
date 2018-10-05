package com.whensunset.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Field {
  String value() default "";
  
  Class asClass() default Object.class;
  
  boolean doAdditionalGet() default false;
}
