package com.github.thundax.bacon.common.security.context;

import org.springframework.beans.BeansException;

public class SpringContextCurrentUserProvider implements CurrentUserProvider {

    private static final String DEFAULT_AUDITOR = "system";

    @Override
    public String currentUserId() {
        try {
            CurrentUserResolver currentUserResolver = SpringBootContext.getBean(CurrentUserResolver.class);
            if (currentUserResolver == null) {
                return DEFAULT_AUDITOR;
            }
            String currentUserId = currentUserResolver.currentUserId();
            return currentUserId == null || currentUserId.isBlank() ? DEFAULT_AUDITOR : currentUserId;
        } catch (BeansException ex) {
            return DEFAULT_AUDITOR;
        }
    }
}
