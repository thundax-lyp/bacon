package com.github.thundax.bacon.common.security.context;

public class DefaultCurrentUserProvider implements CurrentUserProvider {

    private static final String DEFAULT_AUDITOR = "system";

    @Override
    public String currentUserId() {
        return DEFAULT_AUDITOR;
    }
}
