package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxIdMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(OutboxIdMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToOutboxId() {
        assertThat(OutboxIdMapper.toDomain(1003L))
                .isEqualTo(OutboxId.of(1003L));
    }
}
