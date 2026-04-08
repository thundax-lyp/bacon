package com.github.thundax.bacon.common.id.mapper;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperatorIdMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(OperatorIdMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(OperatorIdMapper.toDomain(" ")).isNull();
    }

    @Test
    void shouldConvertValueToOperatorId() {
        assertThat(OperatorIdMapper.toDomain("SYSTEM")).isEqualTo(OperatorId.of("SYSTEM"));
    }
}
