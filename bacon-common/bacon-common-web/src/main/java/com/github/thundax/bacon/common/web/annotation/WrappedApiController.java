package com.github.thundax.bacon.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要使用统一返回包装和异常处理的业务控制器。
 *
 * <p>该注解只应用于面向前端或外部调用方的业务 Controller，不应用于
 * {@code interfaces.provider} 下的 ProviderController。ProviderController
 * 面向内部跨服务调用，应保持原始契约，避免被 ApiResponse 二次包装。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WrappedApiController {
}
