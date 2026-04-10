package com.github.thundax.bacon.common.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantScoped {

    boolean read() default true;

    boolean insert() default true;

    boolean verifyOnUpdate() default true;
}
