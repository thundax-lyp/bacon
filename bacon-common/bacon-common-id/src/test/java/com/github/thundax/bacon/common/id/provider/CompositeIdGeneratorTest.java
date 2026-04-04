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
                new CompositeIdGenerator.NamedIdGenerator("tinyid", bizTag -> 1001L)
        ), bizTag -> 2001L, eventPublisher, true);

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
                new CompositeIdGenerator.NamedIdGenerator("leaf", bizTag -> 1002L)
        ), bizTag -> 2001L, eventPublisher, true);

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
                })
        ), bizTag -> 2001L, eventPublisher, true);

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
                })
        ), bizTag -> {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE, "snowflake down");
        }, eventPublisher, true);

        assertThatThrownBy(() -> generator.nextId("order-id"))
                .isInstanceOf(IdGeneratorException.class)
                .hasMessage("snowflake down");
        assertThat(eventPublisher.events).hasSize(2);
    }

    @Test
    void shouldAllowNullPrimaryGeneratorsAndFallbackToSnowflake() {
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CompositeIdGenerator generator = new CompositeIdGenerator(null, bizTag -> 3001L, eventPublisher, true);

        assertThat(generator.nextId("order-id")).isEqualTo(3001L);
        assertThat(eventPublisher.events).hasSize(1);
        assertThat(eventPublisher.events.get(0).reason()).contains("no primary id generators configured");
    }

    @Test
    void shouldAllowEmptyPrimaryGeneratorsAndFallbackToSnowflake() {
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CompositeIdGenerator generator = new CompositeIdGenerator(List.of(), bizTag -> 3001L, eventPublisher, true);

        assertThat(generator.nextId("order-id")).isEqualTo(3001L);
        assertThat(eventPublisher.events).hasSize(1);
    }

    @Test
    void shouldThrowWhenFallbackDisabledAndPrimaryChainEmpty() {
        CompositeIdGenerator generator = new CompositeIdGenerator(List.of(), bizTag -> 3001L,
                new RecordingEventPublisher(), false);

        assertThatThrownBy(() -> generator.nextId("order-id"))
                .isInstanceOf(IdGeneratorException.class)
                .hasMessage("no primary id generators configured and fallback disabled");
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
