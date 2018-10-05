package com.whensunset.http.retrofit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示API失败时需要重试，并使用指数增加的时间间隔.
 * Interval = initDelay + exponentialBase ^ (retryCount - 1)
 *
 * @Param initDelay 初始重试间隔
 * @Param exponentialBase 指数增加时间的base，单位：秒
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RetryPolicy {
  int initDelay() default 0;
  
  int exponentialBase() default 2;
}
