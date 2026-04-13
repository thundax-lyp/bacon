package com.github.thundax.bacon.common.id.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import org.junit.jupiter.api.Test;

class OperatorIdCodecTest {

    @Test
    void shouldReturnNullWhenLongValueIsNull() {
        assertThat(OperatorIdCodec.toDomainFromLong(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(OperatorIdCodec.toDomain(" ")).isNull();
    }

    @Test
    void shouldConvertValueToOperatorId() {
        assertThat(OperatorIdCodec.toDomain("SYSTEM")).isEqualTo(OperatorId.of("SYSTEM"));
    }

    @Test
    void shouldConvertLongValueToOperatorId() {
        assertThat(OperatorIdCodec.toDomainFromLong(9001L)).isEqualTo(OperatorId.of("9001"));
    }

    @Test
    void shouldConvertOperatorIdToLongValue() {
        assertThat(OperatorIdCodec.toLongValue(OperatorId.of("9001"))).isEqualTo(9001L);
    }
}
