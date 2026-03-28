package com.github.thundax.bacon.common.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 保护内部 Provider 接口，避免被未授权来源直接调用。
 */
public class InternalApiGuardInterceptor implements HandlerInterceptor {

    private final InternalApiGuardProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public InternalApiGuardInterceptor(InternalApiGuardProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.isEnabled() || !matches(request.getRequestURI())) {
            return true;
        }
        String expectedToken = properties.getToken();
        if (!StringUtils.hasText(expectedToken)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Internal API token is not configured");
            return false;
        }
        String actualToken = request.getHeader(properties.getHeaderName());
        if (!StringUtils.hasText(actualToken)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing internal API token");
            return false;
        }
        if (!expectedToken.equals(actualToken)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Internal API token is invalid");
            return false;
        }
        return true;
    }

    private boolean matches(String requestUri) {
        for (String includePathPattern : properties.getIncludePathPatterns()) {
            if (pathMatcher.match(includePathPattern, requestUri)) {
                return true;
            }
        }
        return false;
    }
}
