package com.github.thundax.bacon.product.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductSpuTest {

    @Test
    void shouldCreateDraftProductWithInitialVersion() {
        ProductSpu spu = ProductSpu.create(100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main");

        assertEquals(ProductStatus.DRAFT, spu.getProductStatus());
        assertEquals(1L, spu.getVersion());
    }

    @Test
    void shouldRequireEnabledSkuBeforeOnSale() {
        ProductSpu spu = ProductSpu.create(100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main");
        ProductSku sku = ProductSku.create(200L, 10L, 100L, "SKU-1", "phone black", "{\"color\":\"black\"}",
                BigDecimal.valueOf(99));
        sku.disable();

        assertThrows(ProductDomainException.class, () -> spu.onSale(List.of(sku), 1L));
    }

    @Test
    void shouldMoveToOnSaleAndIncreaseVersionOnce() {
        ProductSpu spu = ProductSpu.create(100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main");
        ProductSku sku = ProductSku.create(200L, 10L, 100L, "SKU-1", "phone black", "{\"color\":\"black\"}",
                BigDecimal.valueOf(99));

        spu.onSale(List.of(sku), 1L);

        assertEquals(ProductStatus.ON_SALE, spu.getProductStatus());
        assertEquals(2L, spu.getVersion());
    }

    @Test
    void shouldRejectEditingArchivedProduct() {
        ProductSpu spu = ProductSpu.create(100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main");
        ProductSku sku = ProductSku.create(200L, 10L, 100L, "SKU-1", "phone black", "{\"color\":\"black\"}",
                BigDecimal.valueOf(99));
        spu.onSale(List.of(sku), 1L);
        spu.archive(2L);

        assertThrows(ProductDomainException.class,
                () -> spu.updateBase("new phone", 20L, "desc", "obj-main", 3L));
        assertEquals(ProductStatus.ARCHIVED, spu.getProductStatus());
    }

    @Test
    void shouldRejectVersionConflict() {
        ProductSpu spu = ProductSpu.create(100L, 10L, "SPU-1", "phone", 20L, "desc", "obj-main");

        assertThrows(ProductDomainException.class,
                () -> spu.updateBase("new phone", 20L, "desc", "obj-main", 99L));
    }
}
