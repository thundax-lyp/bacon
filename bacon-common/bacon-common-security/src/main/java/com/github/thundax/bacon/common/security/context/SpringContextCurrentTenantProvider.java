package com.github.thundax.bacon.common.security.context;

import com.github.thundax.bacon.common.core.context.SpringContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class SpringContextCurrentTenantProvider implements CurrentTenantProvider {

    @Override
    public Long currentTenantId() {
        try {
            CurrentTenantResolver currentTenantResolver = SpringContextHolder.getBean(CurrentTenantResolver.class);
            return currentTenantResolver.currentTenantId();
        } catch (NoSuchBeanDefinitionException ex) {
            return null;
        } catch (BeansException ex) {
            return null;
        }
    }
}
