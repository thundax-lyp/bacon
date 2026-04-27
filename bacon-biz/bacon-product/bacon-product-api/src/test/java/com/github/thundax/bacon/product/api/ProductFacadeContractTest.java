package com.github.thundax.bacon.product.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.product.api.dto.ProductSkuSaleInfoDTO;
import com.github.thundax.bacon.product.api.dto.ProductSnapshotDTO;
import com.github.thundax.bacon.product.api.facade.ProductCommandFacade;
import com.github.thundax.bacon.product.api.facade.ProductReadFacade;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProductFacadeContractTest {

    @Test
    void shouldKeepSkuSaleInfoContract() throws Exception {
        assertField(ProductSkuSaleInfoDTO.class, "tenantId", Long.class);
        assertField(ProductSkuSaleInfoDTO.class, "spuId", Long.class);
        assertField(ProductSkuSaleInfoDTO.class, "spuCode", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "spuName", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "skuId", Long.class);
        assertField(ProductSkuSaleInfoDTO.class, "skuCode", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "skuName", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "categoryId", Long.class);
        assertField(ProductSkuSaleInfoDTO.class, "categoryName", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "specAttributes", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "salePrice", BigDecimal.class);
        assertField(ProductSkuSaleInfoDTO.class, "mainImageObjectId", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "productStatus", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "skuStatus", String.class);
        assertField(ProductSkuSaleInfoDTO.class, "productVersion", Long.class);
        assertField(ProductSkuSaleInfoDTO.class, "saleable", Boolean.class);
        assertField(ProductSkuSaleInfoDTO.class, "failureReason", String.class);

        ProductSkuSaleInfoDTO dto = new ProductSkuSaleInfoDTO(
                10L,
                100L,
                "SPU-1",
                "phone",
                200L,
                "SKU-1",
                "phone black",
                20L,
                "digital",
                "{\"color\":\"black\"}",
                BigDecimal.valueOf(99),
                "obj-main",
                "ON_SALE",
                "ENABLED",
                2L,
                true,
                null);

        assertEquals(200L, dto.skuId());
        assertTrue(dto.saleable());
    }

    @Test
    void shouldKeepSnapshotContract() throws Exception {
        assertField(ProductSnapshotDTO.class, "tenantId", Long.class);
        assertField(ProductSnapshotDTO.class, "snapshotId", Long.class);
        assertField(ProductSnapshotDTO.class, "orderNo", String.class);
        assertField(ProductSnapshotDTO.class, "orderItemNo", String.class);
        assertField(ProductSnapshotDTO.class, "spuId", Long.class);
        assertField(ProductSnapshotDTO.class, "spuCode", String.class);
        assertField(ProductSnapshotDTO.class, "spuName", String.class);
        assertField(ProductSnapshotDTO.class, "skuId", Long.class);
        assertField(ProductSnapshotDTO.class, "skuCode", String.class);
        assertField(ProductSnapshotDTO.class, "skuName", String.class);
        assertField(ProductSnapshotDTO.class, "categoryId", Long.class);
        assertField(ProductSnapshotDTO.class, "categoryName", String.class);
        assertField(ProductSnapshotDTO.class, "specAttributes", String.class);
        assertField(ProductSnapshotDTO.class, "salePrice", BigDecimal.class);
        assertField(ProductSnapshotDTO.class, "quantity", Integer.class);
        assertField(ProductSnapshotDTO.class, "mainImageObjectId", String.class);
        assertField(ProductSnapshotDTO.class, "productVersion", Long.class);
        assertField(ProductSnapshotDTO.class, "createdAt", Instant.class);

        ProductSnapshotDTO dto = new ProductSnapshotDTO(
                10L,
                300L,
                "ORD-1",
                "ITEM-1",
                100L,
                "SPU-1",
                "phone",
                200L,
                "SKU-1",
                "phone black",
                20L,
                "digital",
                "{\"color\":\"black\"}",
                BigDecimal.valueOf(99),
                2,
                "obj-main",
                2L,
                Instant.parse("2026-04-28T00:00:00Z"));

        assertEquals("ITEM-1", dto.orderItemNo());
        assertEquals(2L, dto.productVersion());
    }

    @Test
    void shouldKeepFacadeMethodContracts() throws Exception {
        assertEquals(ProductSkuSaleInfoDTO.class,
                ProductReadFacade.class.getMethod("getSkuSaleInfo", Long.class, Long.class).getReturnType());
        assertEquals(ProductSnapshotDTO.class,
                ProductCommandFacade.class
                        .getMethod("createOrderProductSnapshot", Long.class, String.class, String.class, Long.class,
                                Integer.class)
                        .getReturnType());
    }

    private void assertField(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertEquals(fieldType, field.getType());
    }
}
