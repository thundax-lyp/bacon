package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryIdCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(InventoryIdCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToInventoryId() {
        assertThat(InventoryIdCodec.toDomain(1004L))
                .isEqualTo(InventoryId.of(1004L));
    }
}
