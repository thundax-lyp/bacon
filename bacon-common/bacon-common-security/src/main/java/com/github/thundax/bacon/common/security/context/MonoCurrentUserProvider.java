package com.github.thundax.bacon.common.security.context;

public class MonoCurrentUserProvider implements CurrentUserProvider {

    private final CurrentUserResolver currentUserResolver;

    public MonoCurrentUserProvider(CurrentUserResolver currentUserResolver) {
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public Long currentUserId() {
        return currentUserResolver.currentUserId();
    }
}
