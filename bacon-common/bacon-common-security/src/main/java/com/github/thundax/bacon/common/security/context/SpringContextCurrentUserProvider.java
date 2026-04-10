package com.github.thundax.bacon.common.security.context;

import org.springframework.beans.factory.ObjectProvider;

public class SpringContextCurrentUserProvider implements CurrentUserProvider {

    private final ObjectProvider<CurrentUserResolver> currentUserResolver;

    public SpringContextCurrentUserProvider(ObjectProvider<CurrentUserResolver> currentUserResolver) {
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public Long currentUserId() {
        CurrentUserResolver resolver = currentUserResolver.getIfAvailable();
        if (resolver == null) {
            return 0L;
        }
        Long currentUserId = resolver.currentUserId();
        return currentUserId == null ? 0L : currentUserId;
    }
}
