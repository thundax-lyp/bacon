package com.github.thundax.bacon.common.security.context;

import com.github.thundax.bacon.common.core.context.SpringContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class SpringContextCurrentUserProvider implements CurrentUserProvider {

    private static final String DEFAULT_AUDITOR = "system";

    @Override
    public String currentUserId() {
        try {
            CurrentUserResolver currentUserResolver = SpringContextHolder.getBean(CurrentUserResolver.class);
            String currentUserId = currentUserResolver.currentUserId();
            return currentUserId == null || currentUserId.isBlank() ? DEFAULT_AUDITOR : currentUserId;
        } catch (NoSuchBeanDefinitionException ex) {
            return DEFAULT_AUDITOR;
        } catch (BeansException ex) {
            return DEFAULT_AUDITOR;
        }
    }
}
