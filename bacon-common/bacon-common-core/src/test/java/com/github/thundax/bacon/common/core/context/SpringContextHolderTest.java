package com.github.thundax.bacon.common.core.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.core.exception.SystemException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

class SpringContextHolderTest {

    @AfterEach
    void tearDown() {
        SpringContextHolder.clear();
    }

    @Test
    void shouldProvideBeanOperations() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("sampleBean", SampleBean.class);
        applicationContext.refresh();

        SpringContextHolder springContextHolder = new SpringContextHolder();
        springContextHolder.setApplicationContext(applicationContext);

        assertThat(SpringContextHolder.getApplicationContext()).isSameAs(applicationContext);
        assertThat(SpringContextHolder.getBean(SampleBean.class)).isNotNull();
        assertThat(SpringContextHolder.getBean("sampleBean")).isInstanceOf(SampleBean.class);
        assertThat(SpringContextHolder.getBean("sampleBean", SampleBean.class)).isInstanceOf(SampleBean.class);
        assertThat(SpringContextHolder.containsBean("sampleBean")).isTrue();

        Map<String, SampleBean> beans = SpringContextHolder.getBeansOfType(SampleBean.class);
        assertThat(beans).containsKey("sampleBean");
    }

    @Test
    void shouldFailWhenApplicationContextMissing() {
        assertThatThrownBy(SpringContextHolder::getApplicationContext)
                .isInstanceOf(SystemException.class)
                .hasMessage("Spring application context has not been initialized");
    }

    static class SampleBean {
    }
}
