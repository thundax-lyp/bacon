package com.github.thundax.bacon.common.id.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.id.event.IdFallbackEvent;
import com.github.thundax.bacon.common.id.exception.IdGeneratorErrorCode;
import com.github.thundax.bacon.common.id.exception.IdGeneratorException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class CompositeIdGeneratorTest {

    @Test
    void shouldUseFirstGeneratorWhenAvailable() {
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CompositeIdGenerator generator = new CompositeIdGenerator(List.of(
                new CompositeIdGenerator.NamedIdGenerator("tinyid", bizTag -> 1001L),
                new CompositeIdGenerator.NamedIdGenerator("snowflake", bizTag -> 2001L)
        ), eventPublisher);

        assertThat(generator.nextId("order-id")).isEqualTo(1001L);
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void shouldFallbackToNextGeneratorAndPublishEvent() {
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CompositeIdGenerator generator = new CompositeIdGenerator(List.of(
                new CompositeIdGenerator.NamedIdGenerator("tinyid", bizTag -> {
                    throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE, "tinyid down");
                }),
                new CompositeIdGenerator.NamedIdGenerator("leaf", bizTag -> 1002L),
                new CompositeIdGenerator.NamedIdGenerator("snowflake", bizTag -> 2001L)
        ), eventPublisher);

        assertThat(generator.nextId("order-id")).isEqualTo(1002L);
        assertThat(eventPublisher.events).hasSize(1);
        IdFallbackEvent event = eventPublisher.events.get(0);
        assertThat(event.reason()).contains("tinyid");
        assertThat(event.reason()).contains("fallback to leaf");
    }

    @Test
    void shouldFallbackToSnowflakeAfterRemoteProvidersFail() {
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CompositeIdGenerator generator = new CompositeIdGenerator(List.of(
                new CompositeIdGenerator.NamedIdGenerator("tinyid", bizTag -> {
                    throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE, "tinyid down");
                }),
                new CompositeIdGenerator.NamedIdGenerator("leaf", bizTag -> {
                    throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE, "leaf down");
                }),
                new CompositeIdGenerator.NamedIdGenerator("snowflake", bizTag -> 2001L)
        ), eventPublisher);

        assertThat(generator.nextId("order-id")).isEqualTo(2001L);
        assertThat(eventPublisher.events).hasSize(2);
        assertThat(eventPublisher.events.get(0).reason()).contains("fallback to leaf");
        assertThat(eventPublisher.events.get(1).reason()).contains("fallback to snowflake");
    }

    @Test
    void shouldThrowLastExceptionWhenAllGeneratorsFail() {
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CompositeIdGenerator generator = new CompositeIdGenerator(List.of(
                new CompositeIdGenerator.NamedIdGenerator("tinyid", bizTag -> {
                    throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE, "tinyid down");
                }),
                new CompositeIdGenerator.NamedIdGenerator("snowflake", bizTag -> {
                    throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE, "snowflake down");
                })
        ), eventPublisher);

        assertThatThrownBy(() -> generator.nextId("order-id"))
                .isInstanceOf(IdGeneratorException.class)
                .hasMessage("snowflake down");
        assertThat(eventPublisher.events).hasSize(1);
    }

    @Test
    void shouldAllowNullGeneratorsAndFailLazily() {
        CompositeIdGenerator generator = new CompositeIdGenerator(null, new RecordingEventPublisher());

        assertThatThrownBy(() -> generator.nextId("order-id"))
                .isInstanceOf(IdGeneratorException.class)
                .hasMessage("no id generators configured");
    }

    @Test
    void shouldAllowEmptyGeneratorsAndFailLazily() {
        CompositeIdGenerator generator = new CompositeIdGenerator(List.of(), new RecordingEventPublisher());

        assertThatThrownBy(() -> generator.nextId("order-id"))
                .isInstanceOf(IdGeneratorException.class)
                .hasMessage("no id generators configured");
    }

    private static final class RecordingEventPublisher implements ApplicationEventPublisher {

        private final List<IdFallbackEvent> events = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            if (event instanceof IdFallbackEvent fallbackEvent) {
                events.add(fallbackEvent);
            }
        }
    }
}
