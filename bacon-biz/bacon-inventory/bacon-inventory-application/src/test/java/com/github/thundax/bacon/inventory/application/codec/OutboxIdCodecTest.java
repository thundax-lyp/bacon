package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxIdCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(OutboxIdCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToOutboxId() {
        assertThat(OutboxIdCodec.toDomain(1003L))
                .isEqualTo(OutboxId.of(1003L));
    }
}
