package com.github.thundax.bacon.inventory.application.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import org.junit.jupiter.api.Test;

class EventCodeCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(EventCodeCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(EventCodeCodec.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToEventCode() {
        assertThat(EventCodeCodec.toDomain("EVENT-001")).isEqualTo(EventCode.of("EVENT-001"));
    }
}
