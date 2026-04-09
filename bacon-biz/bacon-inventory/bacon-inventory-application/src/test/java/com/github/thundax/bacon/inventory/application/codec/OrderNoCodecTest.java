package com.github.thundax.bacon.inventory.application.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import org.junit.jupiter.api.Test;

class OrderNoCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(OrderNoCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(OrderNoCodec.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToOrderNo() {
        OrderNo result = OrderNoCodec.toDomain("ORDER-001");

        assertThat(result).isEqualTo(OrderNo.of("ORDER-001"));
    }
}
