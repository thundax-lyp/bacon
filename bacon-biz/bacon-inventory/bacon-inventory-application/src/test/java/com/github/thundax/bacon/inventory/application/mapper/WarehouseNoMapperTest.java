package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseNoMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(WarehouseNoMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(WarehouseNoMapper.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToWarehouseNo() {
        assertThat(WarehouseNoMapper.toDomain("WH-001"))
                .isEqualTo(WarehouseNo.of("WH-001"));
    }
}
