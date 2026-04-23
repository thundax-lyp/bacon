package com.github.thundax.bacon.inventory.infra.facade.remote.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationItemFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class InventoryFacadeRemoteContractTest {

    private static final String BASE_URL = "http://inventory.test/api";
    private static final String PROVIDER_TOKEN = "inventory-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("X-Bacon-Provider-Token", PROVIDER_TOKEN);
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallInventoryReadProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/inventory/queries/available-stock?skuId=101"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(stockJson(101), MediaType.APPLICATION_JSON));
        server.expect(request -> {
                    assertThat(request.getURI().getPath()).isEqualTo("/api/providers/inventory/queries/available-stocks");
                    assertThat(request.getURI().getQuery()).contains("skuIds=101").contains("skuIds=102");
                })
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[" + stockJson(101) + "," + stockJson(102) + "]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/inventory/queries/reservation?orderNo=ORD-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(reservationJson("RESERVED"), MediaType.APPLICATION_JSON));
        InventoryReadFacadeRemoteImpl facade = new InventoryReadFacadeRemoteImpl(restClientBuilder.build());

        InventoryStockFacadeResponse stock = facade.getAvailableStock(new InventoryAvailableStockFacadeRequest(101L));
        InventoryStockListFacadeResponse stocks = facade.batchGetAvailableStock(
                new InventoryBatchAvailableStockFacadeRequest(new LinkedHashSet<>(List.of(101L, 102L))));
        InventoryReservationFacadeResponse reservation =
                facade.getReservationByOrderNo(new InventoryReservationGetFacadeRequest("ORD-1"));

        assertThat(stock.getAvailableQuantity()).isEqualTo(80);
        assertThat(stocks.getRecords()).extracting(InventoryStockFacadeResponse::getSkuId).containsExactly(101L, 102L);
        assertThat(reservation.getInventoryStatus()).isEqualTo("RESERVED");
        assertThat(reservation.getItems()).hasSize(1);
        server.verify();
    }

    @Test
    void shouldCallInventoryCommandProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/inventory/commands/reserve"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(reservationJson("RESERVED"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/inventory/commands/release"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(reservationJson("RELEASED"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/inventory/commands/deduct"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(reservationJson("DEDUCTED"), MediaType.APPLICATION_JSON));
        InventoryCommandFacadeRemoteImpl facade = new InventoryCommandFacadeRemoteImpl(restClientBuilder.build());

        InventoryReservationFacadeResponse reserved = facade.reserveStock(new InventoryReserveFacadeRequest(
                "ORD-1", List.of(new InventoryReservationItemFacadeRequest(101L, 2))));
        InventoryReservationFacadeResponse released =
                facade.releaseReservedStock(new InventoryReleaseFacadeRequest("ORD-1", "TIMEOUT_CLOSED"));
        InventoryReservationFacadeResponse deducted =
                facade.deductReservedStock(new InventoryDeductFacadeRequest("ORD-1"));

        assertThat(reserved.getInventoryStatus()).isEqualTo("RESERVED");
        assertThat(released.getInventoryStatus()).isEqualTo("RELEASED");
        assertThat(deducted.getInventoryStatus()).isEqualTo("DEDUCTED");
        server.verify();
    }

    private String stockJson(long skuId) {
        return """
                {
                  "skuId": %d,
                  "warehouseCode": "DEFAULT",
                  "onHandQuantity": 100,
                  "reservedQuantity": 20,
                  "availableQuantity": 80,
                  "status": "ENABLED",
                  "updatedAt": "2026-03-26T10:00:00Z"
                }
                """
                .formatted(skuId);
    }

    private String reservationJson(String status) {
        String releaseReason = "RELEASED".equals(status) ? "\"TIMEOUT_CLOSED\"" : "null";
        return """
                {
                  "orderNo": "ORD-1",
                  "reservationNo": "RSV-1",
                  "reservationStatus": "%s",
                  "inventoryStatus": "%s",
                  "warehouseCode": "DEFAULT",
                  "items": [
                    {"skuId": 101, "quantity": 2}
                  ],
                  "failureReason": null,
                  "releaseReason": %s,
                  "createdAt": "2026-03-26T10:00:00Z",
                  "releasedAt": null,
                  "deductedAt": null
                }
                """
                .formatted(status, status, releaseReason);
    }
}
