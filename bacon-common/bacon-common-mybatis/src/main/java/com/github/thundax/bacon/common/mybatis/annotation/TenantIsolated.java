package com.github.thundax.bacon.common.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要租户隔离的持久化 DO。
 * 挂上该注解后：
 * 1. 查询在上下文存在 tenantId 时自动追加 tenant_id 过滤
 * 2. 新增在字段 tenantId 为空且上下文存在 tenantId 时自动回填
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantIsolated {}
