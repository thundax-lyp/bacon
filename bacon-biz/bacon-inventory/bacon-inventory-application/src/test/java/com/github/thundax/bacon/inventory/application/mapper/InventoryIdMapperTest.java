package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryIdMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(InventoryIdMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToInventoryId() {
        assertThat(InventoryIdMapper.toDomain(1004L))
                .isEqualTo(InventoryId.of(1004L));
    }
}
