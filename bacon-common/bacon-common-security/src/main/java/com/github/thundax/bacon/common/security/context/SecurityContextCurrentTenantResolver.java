package com.github.thundax.bacon.common.security.context;

import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityContextCurrentTenantResolver implements CurrentTenantResolver {

    @Override
    public Long currentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Long tenantId = extractTenantId(authentication.getPrincipal());
        if (tenantId != null) {
            return tenantId;
        }
        tenantId = extractTenantId(authentication.getDetails());
        if (tenantId != null) {
            return tenantId;
        }
        return extractTenantId(authentication);
    }

    private Long extractTenantId(Object source) {
        if (source == null || source instanceof String || source instanceof UserDetails) {
            return null;
        }
        if (source instanceof Map<?, ?> map) {
            return toLong(firstNonNull(map, "tenantId", "tenant_id", "tenant"));
        }
        Long tenantId = invokeLongMethod(source, "getTenantId");
        if (tenantId != null) {
            return tenantId;
        }
        tenantId = invokeLongMethod(source, "tenantId");
        if (tenantId != null) {
            return tenantId;
        }
        Object tenant = invokeObjectMethod(source, "getTenant");
        return toLong(tenant);
    }

    private Object firstNonNull(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private Long invokeLongMethod(Object target, String methodName) {
        Object value = invokeObjectMethod(target, methodName);
        return toLong(value);
    }

    private Object invokeObjectMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value);
        if (text.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
