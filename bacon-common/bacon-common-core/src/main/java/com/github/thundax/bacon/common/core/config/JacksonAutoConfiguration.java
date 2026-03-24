package com.github.thundax.bacon.common.core.config;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.math.BigInteger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 创建全局 Jackson 定制器，用于统一 JSON 序列化与反序列化规则。
 */
@AutoConfiguration
public class JacksonAutoConfiguration {

    /**
     * 创建 Jackson2ObjectMapperBuilderCustomizer，统一 Long、大整数和 Java 时间类型的 JSON 处理方式。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modulesToInstall(JavaTimeModule.class)
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance)
                .serializerByType(BigInteger.class, ToStringSerializer.instance)
                .serializers(
                        new LocalDateTimeSerializer(DatePattern.NORM_DATETIME_FORMATTER),
                        new LocalDateSerializer(DatePattern.NORM_DATE_FORMATTER),
                        new LocalTimeSerializer(DatePattern.NORM_TIME_FORMATTER)
                )
                .deserializers(
                        new LocalDateTimeDeserializer(DatePattern.NORM_DATETIME_FORMATTER),
                        new LocalDateDeserializer(DatePattern.NORM_DATE_FORMATTER),
                        new LocalTimeDeserializer(DatePattern.NORM_TIME_FORMATTER)
                );
    }
}
