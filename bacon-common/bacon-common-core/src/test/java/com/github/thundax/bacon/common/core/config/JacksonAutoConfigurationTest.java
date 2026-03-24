package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class JacksonAutoConfigurationTest {

    private final JacksonAutoConfiguration jacksonAutoConfiguration = new JacksonAutoConfiguration();

    @Test
    void shouldSerializeLongAsStringAndFormatJavaTimeTypes() throws Exception {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        jacksonAutoConfiguration.jackson2ObjectMapperBuilderCustomizer().customize(builder);
        ObjectMapper objectMapper = builder.build();

        DemoPayload payload = new DemoPayload(
                9007199254740993L,
                LocalDateTime.of(2026, 3, 24, 12, 30, 45),
                LocalDate.of(2026, 3, 24),
                LocalTime.of(12, 30, 45),
                null
        );

        String json = objectMapper.writeValueAsString(payload);

        assertThat(json).contains("\"id\":\"9007199254740993\"");
        assertThat(json).contains("\"createdAt\":\"2026-03-24 12:30:45\"");
        assertThat(json).contains("\"bizDate\":\"2026-03-24\"");
        assertThat(json).contains("\"bizTime\":\"12:30:45\"");
        assertThat(json).doesNotContain("nullableField");
    }

    private record DemoPayload(Long id, LocalDateTime createdAt, LocalDate bizDate, LocalTime bizTime,
                               String nullableField) {
    }
}
