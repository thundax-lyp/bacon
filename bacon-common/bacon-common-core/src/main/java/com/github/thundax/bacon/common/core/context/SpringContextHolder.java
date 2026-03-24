package com.github.thundax.bacon.common.core.context;

import com.github.thundax.bacon.common.core.exception.SystemException;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文持有器，用于在非 Spring 管理对象中获取 Bean 和查询容器状态。
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        ApplicationContext context = applicationContext;
        if (context == null) {
            throw new SystemException("Spring application context has not been initialized");
        }
        return context;
    }

    public static <T> T getBean(Class<T> beanType) {
        return getApplicationContext().getBean(beanType);
    }

    public static Object getBean(String beanName) {
        return getApplicationContext().getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> beanType) {
        return getApplicationContext().getBean(beanName, beanType);
    }

    public static boolean containsBean(String beanName) {
        ApplicationContext context = applicationContext;
        return context != null && context.containsBean(beanName);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> beanType) {
        return getApplicationContext().getBeansOfType(beanType);
    }

    public static void clear() {
        applicationContext = null;
    }
}
