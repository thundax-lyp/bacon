package com.github.thundax.bacon.common.commerce.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class WarehouseCodeCodecTest {

    @Test
    void shouldReturnNullForNullOrBlank() {
        assertNull(WarehouseCodeCodec.toDomain(null));
        assertNull(WarehouseCodeCodec.toDomain("   "));
        assertNull(WarehouseCodeCodec.toValue(null));
    }

    @Test
    void shouldConvertWarehouseCode() {
        assertEquals("WH-001", WarehouseCodeCodec.toDomain("WH-001").value());
        assertEquals("WH-002", WarehouseCodeCodec.toValue(WarehouseCodeCodec.toDomain("WH-002")));
    }
}
