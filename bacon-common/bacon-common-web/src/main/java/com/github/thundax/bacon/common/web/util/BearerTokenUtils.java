package com.github.thundax.bacon.common.web.util;

public final class BearerTokenUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    private BearerTokenUtils() {
    }

    public static String extractToken(String authorization) {
        return authorization != null && authorization.startsWith(BEARER_PREFIX)
                ? authorization.substring(BEARER_PREFIX.length())
                : authorization;
    }
}
