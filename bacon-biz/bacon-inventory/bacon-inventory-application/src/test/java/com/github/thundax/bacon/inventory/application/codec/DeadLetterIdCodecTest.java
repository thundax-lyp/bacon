package com.github.thundax.bacon.inventory.application.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import org.junit.jupiter.api.Test;

class DeadLetterIdCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(DeadLetterIdCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToDeadLetterId() {
        assertThat(DeadLetterIdCodec.toDomain(1001L)).isEqualTo(DeadLetterId.of(1001L));
    }
}
