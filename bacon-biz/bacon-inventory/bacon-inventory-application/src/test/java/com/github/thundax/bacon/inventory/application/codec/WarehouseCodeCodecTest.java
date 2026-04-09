package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.common.core.valueobject.WarehouseCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseCodeCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(WarehouseCodeCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(WarehouseCodeCodec.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToWarehouseCode() {
        assertThat(WarehouseCodeCodec.toDomain("WH-001"))
                .isEqualTo(WarehouseCode.of("WH-001"));
    }
}
