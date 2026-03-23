package com.github.thundax.bacon.common.log.annotation;

import com.github.thundax.bacon.common.log.LogEventType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {

    String module();

    String action();

    LogEventType eventType() default LogEventType.OTHER;
}
