package com.github.thundax.bacon.common.web.resolver;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.web.annotation.CurrentTenant;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentTenantArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(CurrentTenant.class)) {
            return false;
        }
        Class<?> parameterType = parameter.getParameterType();
        return Long.class.equals(parameterType) || long.class.equals(parameterType);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Long tenantId = BaconContextHolder.currentTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required for current request");
        }
        return tenantId;
    }
}
