package com.github.thundax.bacon.common.web.context;

import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class DefaultBaconContextResolver implements BaconContextResolver {

    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public BaconContext resolve(HttpServletRequest request) {
        return new BaconContext(resolveTenantId(request), resolveUserId(request));
    }

    protected Long resolveTenantId(HttpServletRequest request) {
        Long tenantIdFromHeader = toLong(request == null ? null : request.getHeader(TENANT_ID_HEADER));
        if (tenantIdFromHeader != null) {
            return tenantIdFromHeader;
        }
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
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

    protected Long resolveUserId(HttpServletRequest request) {
        Long userIdFromHeader = toLong(request == null ? null : request.getHeader(USER_ID_HEADER));
        if (userIdFromHeader != null) {
            return userIdFromHeader;
        }
        Authentication authentication = currentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Long userId = extractUserId(authentication.getPrincipal());
        if (userId != null) {
            return userId;
        }
        userId = extractUserId(authentication.getDetails());
        if (userId != null) {
            return userId;
        }
        return extractUserId(authentication);
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof String principal
                && "anonymousUser".equalsIgnoreCase(principal)) {
            return null;
        }
        return authentication;
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

    private Long extractUserId(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof String sourceText) {
            return toLong(sourceText);
        }
        if (source instanceof UserDetails userDetails) {
            return toLong(userDetails.getUsername());
        }
        if (source instanceof Principal principal) {
            return toLong(principal.getName());
        }
        if (source instanceof Map<?, ?> map) {
            return toLong(firstNonNull(map, "userId", "user_id", "user"));
        }
        Long userId = invokeLongMethod(source, "getUserId");
        if (userId != null) {
            return userId;
        }
        userId = invokeLongMethod(source, "userId");
        if (userId != null) {
            return userId;
        }
        return invokeLongMethod(source, "getId");
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
