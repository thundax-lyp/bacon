package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class RedisAutoConfigurationTest {

    private final RedisAutoConfiguration redisAutoConfiguration = new RedisAutoConfiguration();
    private final JacksonAutoConfiguration jacksonAutoConfiguration = new JacksonAutoConfiguration();

    @Test
    void shouldConfigureRedisTemplateWithStringAndJsonSerializers() {
        RedisConnectionFactory redisConnectionFactory = createRedisConnectionFactory();
        ObjectMapper objectMapper = createObjectMapper();

        RedisTemplate<String, Object> redisTemplate =
                redisAutoConfiguration.redisTemplate(redisConnectionFactory, objectMapper);

        assertThat(redisTemplate.getConnectionFactory()).isSameAs(redisConnectionFactory);
        assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
        assertThat(redisTemplate.getHashValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
        assertThat(redisTemplate.isEnableDefaultSerializer()).isTrue();
    }

    private ObjectMapper createObjectMapper() {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        jacksonAutoConfiguration.jackson2ObjectMapperBuilderCustomizer().customize(builder);
        return builder.build();
    }

    private RedisConnectionFactory createRedisConnectionFactory() {
        return (RedisConnectionFactory) Proxy.newProxyInstance(
                RedisConnectionFactory.class.getClassLoader(),
                new Class[] {RedisConnectionFactory.class},
                (proxy, method, args) -> null);
    }
}
