package com.github.thundax.bacon.common.web.resolver;

import com.github.thundax.bacon.common.security.context.CurrentTenantProvider;
import com.github.thundax.bacon.common.web.annotation.CurrentTenant;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentTenantArgumentResolver implements HandlerMethodArgumentResolver {

    private final CurrentTenantProvider currentTenantProvider;

    public CurrentTenantArgumentResolver(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(CurrentTenant.class)) {
            return false;
        }
        Class<?> parameterType = parameter.getParameterType();
        return Long.class.equals(parameterType) || long.class.equals(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Long tenantId = currentTenantProvider.currentTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required in security context");
        }
        return tenantId;
    }
}
