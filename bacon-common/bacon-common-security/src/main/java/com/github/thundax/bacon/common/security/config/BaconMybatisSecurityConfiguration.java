package com.github.thundax.bacon.common.security.config;

import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Method;
import java.security.Principal;

@Configuration(proxyBeanMethods = false)
public class BaconMybatisSecurityConfiguration {

    private static final String DEFAULT_AUDITOR = "system";

    @Bean
    public CurrentUserProvider currentUserProvider() {
        return this::resolveCurrentUserId;
    }

    private String resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return DEFAULT_AUDITOR;
        }

        Object principal = authentication.getPrincipal();
        String auditor = extractUserId(principal);
        if (auditor != null && !auditor.isBlank()) {
            return auditor;
        }

        String name = authentication.getName();
        return name == null || name.isBlank() ? DEFAULT_AUDITOR : name;
    }

    private String extractUserId(Object principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof String principalText) {
            if ("anonymousUser".equalsIgnoreCase(principalText)) {
                return null;
            }
            return principalText;
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof Principal securityPrincipal) {
            return securityPrincipal.getName();
        }

        String userId = invokeStringMethod(principal, "getUserId");
        if (userId != null && !userId.isBlank()) {
            return userId;
        }
        return invokeStringMethod(principal, "getId");
    }

    private String invokeStringMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
}
