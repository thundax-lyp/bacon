package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseNoCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(WarehouseNoCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(WarehouseNoCodec.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToWarehouseNo() {
        assertThat(WarehouseNoCodec.toDomain("WH-001"))
                .isEqualTo(WarehouseNo.of("WH-001"));
    }
}
