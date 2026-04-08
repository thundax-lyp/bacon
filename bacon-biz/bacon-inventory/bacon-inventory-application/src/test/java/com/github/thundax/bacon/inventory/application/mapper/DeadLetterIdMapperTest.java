package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeadLetterIdMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(DeadLetterIdMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToDeadLetterId() {
        assertThat(DeadLetterIdMapper.toDomain(1001L))
                .isEqualTo(DeadLetterId.of(1001L));
    }
}
