package com.github.thundax.bacon.common.security.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringBootContext implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBootContext.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> beanType) {
        ApplicationContext context = applicationContext;
        if (context == null) {
            return null;
        }
        return context.getBean(beanType);
    }
}
