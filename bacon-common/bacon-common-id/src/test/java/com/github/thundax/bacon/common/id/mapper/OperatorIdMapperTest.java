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

    @Test
    void shouldConvertLongValueToOperatorId() {
        assertThat(OperatorIdMapper.toDomain(9001L)).isEqualTo(OperatorId.of("9001"));
    }

    @Test
    void shouldConvertOperatorIdToLongValue() {
        assertThat(OperatorIdMapper.toLongValue(OperatorId.of("9001"))).isEqualTo(9001L);
    }
}
