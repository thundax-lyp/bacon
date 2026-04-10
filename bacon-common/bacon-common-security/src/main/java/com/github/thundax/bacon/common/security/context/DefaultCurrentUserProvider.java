package com.github.thundax.bacon.common.security.context;

public class DefaultCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long currentUserId() {
        return 0L;
    }
}
