package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderNoMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(OrderNoMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(OrderNoMapper.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToOrderNo() {
        OrderNo result = OrderNoMapper.toDomain("ORDER-001");

        assertThat(result).isEqualTo(OrderNo.of("ORDER-001"));
    }
}
