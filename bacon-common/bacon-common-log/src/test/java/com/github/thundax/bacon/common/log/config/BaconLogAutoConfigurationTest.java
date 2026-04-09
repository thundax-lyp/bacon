package com.github.thundax.bacon.common.log.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.log.producer.MqSysLogMessageProducer;
import com.github.thundax.bacon.common.log.producer.NoOpSysLogMessageProducer;
import com.github.thundax.bacon.common.log.producer.SysLogMessageProducer;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import com.github.thundax.bacon.common.mq.config.BaconMqAutoConfiguration;
import com.github.thundax.bacon.common.mq.support.NoOpBaconMqSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BaconLogAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BaconMqAutoConfiguration.class, BaconLogAutoConfiguration.class));

    @Test
    void shouldRegisterMqSysLogProducerWhenMqSenderFallsBackToNoOp() {
        contextRunner.withPropertyValues("bacon.mq.enabled=false").run(context -> {
            assertThat(context).hasSingleBean(BaconMqSender.class);
            assertThat(context.getBean(BaconMqSender.class)).isInstanceOf(NoOpBaconMqSender.class);
            assertThat(context).hasSingleBean(MqSysLogMessageProducer.class);
            assertThat(context.getBean(SysLogMessageProducer.class)).isInstanceOf(MqSysLogMessageProducer.class);
            assertThat(context).hasBean("noOpSysLogMessageProducer");
            assertThat(context.getBean("noOpSysLogMessageProducer")).isInstanceOf(NoOpSysLogMessageProducer.class);
        });
    }
}
