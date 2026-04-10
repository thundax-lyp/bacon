package com.github.thundax.bacon.common.security.context;

import org.springframework.beans.factory.ObjectProvider;

public class SpringContextCurrentUserProvider implements CurrentUserProvider {

    private static final String DEFAULT_AUDITOR = "system";
    private final ObjectProvider<CurrentUserResolver> currentUserResolver;

    public SpringContextCurrentUserProvider(ObjectProvider<CurrentUserResolver> currentUserResolver) {
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public String currentUserId() {
        CurrentUserResolver resolver = currentUserResolver.getIfAvailable();
        if (resolver == null) {
            return DEFAULT_AUDITOR;
        }
        String currentUserId = resolver.currentUserId();
        return currentUserId == null || currentUserId.isBlank() ? DEFAULT_AUDITOR : currentUserId;
    }
}
