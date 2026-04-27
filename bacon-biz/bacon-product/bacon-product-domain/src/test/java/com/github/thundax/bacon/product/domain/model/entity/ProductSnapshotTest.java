package com.github.thundax.bacon.product.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductSnapshotTest {

    @Test
    void shouldCreateImmutableSnapshotFromCurrentProductVersion() {
        ProductSpu spu = ProductSpu.create(100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main");
        ProductSku sku = ProductSku.create(200L, 10L, 100L, "SKU-1", "phone black", "{\"color\":\"black\"}",
                BigDecimal.valueOf(99));
        spu.onSale(List.of(sku), 1L);

        ProductSnapshot snapshot = ProductSnapshot.create(300L, "ORD-1", "ITEM-1", spu, sku, "digital", 2);

        assertEquals("ORD-1", snapshot.orderNo());
        assertEquals("ITEM-1", snapshot.orderItemNo());
        assertEquals("phone", snapshot.spuName());
        assertEquals(BigDecimal.valueOf(99), snapshot.salePrice());
        assertEquals(2L, snapshot.productVersion());
    }

    @Test
    void shouldRejectSnapshotWhenProductIsNotOnSale() {
        ProductSpu spu = ProductSpu.reconstruct(
                100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main", ProductStatus.OFF_SALE, 2L);
        ProductSku sku = ProductSku.create(200L, 10L, 100L, "SKU-1", "phone black", "{\"color\":\"black\"}",
                BigDecimal.valueOf(99));

        assertThrows(ProductDomainException.class,
                () -> ProductSnapshot.create(300L, "ORD-1", "ITEM-1", spu, sku, "digital", 2));
    }

    @Test
    void shouldRejectSnapshotWhenSkuIsDisabled() {
        ProductSpu spu = ProductSpu.create(100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main");
        ProductSku sku = ProductSku.create(200L, 10L, 100L, "SKU-1", "phone black", "{\"color\":\"black\"}",
                BigDecimal.valueOf(99));
        spu.onSale(List.of(sku), 1L);
        sku.disable();

        assertThrows(ProductDomainException.class,
                () -> ProductSnapshot.create(300L, "ORD-1", "ITEM-1", spu, sku, "digital", 2));
    }
}
