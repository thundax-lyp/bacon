package com.github.thundax.bacon.common.security.context;

import java.lang.reflect.Method;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityContextCurrentUserResolver implements CurrentUserResolver {

    @Override
    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        Long userId = extractUserId(principal);
        if (userId != null) {
            return userId;
        }

        return toLong(authentication.getName());
    }

    private Long extractUserId(Object principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof String principalText) {
            if ("anonymousUser".equalsIgnoreCase(principalText)) {
                return null;
            }
            return toLong(principalText);
        }
        if (principal instanceof UserDetails userDetails) {
            return toLong(userDetails.getUsername());
        }
        if (principal instanceof Principal securityPrincipal) {
            return toLong(securityPrincipal.getName());
        }

        Long userId = invokeLongMethod(principal, "getUserId");
        if (userId != null) {
            return userId;
        }
        return invokeLongMethod(principal, "getId");
    }

    private Long invokeLongMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return toLong(value);
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
