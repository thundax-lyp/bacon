package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventCodeMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(EventCodeMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(EventCodeMapper.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToEventCode() {
        assertThat(EventCodeMapper.toDomain("EVENT-001"))
                .isEqualTo(EventCode.of("EVENT-001"));
    }
}
