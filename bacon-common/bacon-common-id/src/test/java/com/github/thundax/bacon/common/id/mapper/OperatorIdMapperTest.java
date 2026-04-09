package com.github.thundax.bacon.common.id.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import org.junit.jupiter.api.Test;

class OperatorIdMapperTest {

    @Test
    void shouldReturnNullWhenLongValueIsNull() {
        assertThat(OperatorIdMapper.toDomainFromLong(null)).isNull();
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
        assertThat(OperatorIdMapper.toDomainFromLong(9001L)).isEqualTo(OperatorId.of("9001"));
    }

    @Test
    void shouldConvertOperatorIdToLongValue() {
        assertThat(OperatorIdMapper.toLongValue(OperatorId.of("9001"))).isEqualTo(9001L);
    }
}
