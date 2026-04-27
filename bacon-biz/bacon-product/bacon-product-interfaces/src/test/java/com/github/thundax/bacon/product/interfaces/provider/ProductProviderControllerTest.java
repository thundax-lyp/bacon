package com.github.thundax.bacon.product.interfaces.provider;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.product.application.result.ProductSkuSaleInfoResult;
import com.github.thundax.bacon.product.application.result.ProductSnapshotResult;
import com.github.thundax.bacon.product.application.service.ProductSearchApplicationService;
import com.github.thundax.bacon.product.application.service.ProductSnapshotApplicationService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductProviderControllerTest {

    @Test
    void shouldReturnSkuSaleInfo() throws Exception {
        ProductSearchApplicationService searchApplicationService =
                Mockito.mock(ProductSearchApplicationService.class);
        ProductSnapshotApplicationService snapshotApplicationService =
                Mockito.mock(ProductSnapshotApplicationService.class);
        Mockito.when(searchApplicationService.getSkuSaleInfo(200L)).thenReturn(new ProductSkuSaleInfoResult(
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
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductProviderController(searchApplicationService, snapshotApplicationService))
                .build();

        mockMvc.perform(get("/providers/products/skus/{skuId}/sale-info", 200L).param("tenantId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuId").value(200))
                .andExpect(jsonPath("$.saleable").value(true));
    }

    @Test
    void shouldCreateOrderProductSnapshot() throws Exception {
        ProductSearchApplicationService searchApplicationService =
                Mockito.mock(ProductSearchApplicationService.class);
        ProductSnapshotApplicationService snapshotApplicationService =
                Mockito.mock(ProductSnapshotApplicationService.class);
        Mockito.when(snapshotApplicationService.createOrderProductSnapshot(ArgumentMatchers.any()))
                .thenReturn(snapshotResult());
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductProviderController(searchApplicationService, snapshotApplicationService))
                .build();

        mockMvc.perform(post("/providers/products/snapshots/order-items")
                        .param("tenantId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"ORD-1\",\"orderItemNo\":\"ITEM-1\",\"skuId\":200,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshotId").value(300))
                .andExpect(jsonPath("$.orderItemNo").value("ITEM-1"));
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
