package com.github.thundax.bacon.product.interfaces.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.product.api.response.ProductSkuSaleInfoFacadeResponse;
import com.github.thundax.bacon.product.api.response.ProductOrderSnapshotCreateFacadeResponse;
import com.github.thundax.bacon.product.api.request.ProductSkuSaleInfoFacadeRequest;
import com.github.thundax.bacon.product.api.request.ProductOrderSnapshotCreateFacadeRequest;
import com.github.thundax.bacon.product.application.command.ProductSnapshotApplicationService;
import com.github.thundax.bacon.product.application.query.ProductSearchApplicationService;
import com.github.thundax.bacon.product.application.result.ProductSkuSaleInfoResult;
import com.github.thundax.bacon.product.application.result.ProductSnapshotResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductFacadeLocalContractTest {

    @Test
    void shouldAdaptReadFacadeToApplicationService() {
        ProductSearchApplicationService searchApplicationService =
                org.mockito.Mockito.mock(ProductSearchApplicationService.class);
        when(searchApplicationService.getSkuSaleInfo(200L)).thenReturn(new ProductSkuSaleInfoResult(
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
                null));
        ProductReadFacadeLocalImpl facade = new ProductReadFacadeLocalImpl(searchApplicationService);

        ProductSkuSaleInfoFacadeResponse response = facade.getSkuSaleInfo(new ProductSkuSaleInfoFacadeRequest(10L, 200L));

        assertEquals(200L, response.skuSaleInfo().skuId());
        assertEquals("ON_SALE", response.skuSaleInfo().productStatus());
        verify(searchApplicationService).getSkuSaleInfo(200L);
    }

    @Test
    void shouldAdaptCommandFacadeToApplicationService() {
        ProductSnapshotApplicationService snapshotApplicationService =
                org.mockito.Mockito.mock(ProductSnapshotApplicationService.class);
        when(snapshotApplicationService.createOrderProductSnapshot(argThat(command ->
                        command != null && command.orderNo().equals("ORD-1") && command.orderItemNo().equals("ITEM-1"))))
                .thenReturn(snapshotResult());
        ProductCommandFacadeLocalImpl facade = new ProductCommandFacadeLocalImpl(snapshotApplicationService);

        ProductOrderSnapshotCreateFacadeResponse response = facade.createOrderProductSnapshot(
                new ProductOrderSnapshotCreateFacadeRequest(10L, "ORD-1", "ITEM-1", 200L, 2));

        assertEquals(300L, response.snapshot().snapshotId());
        assertEquals("ITEM-1", response.snapshot().orderItemNo());
    }

    private ProductSnapshotResult snapshotResult() {
        return new ProductSnapshotResult(
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
                2L);
    }
}
