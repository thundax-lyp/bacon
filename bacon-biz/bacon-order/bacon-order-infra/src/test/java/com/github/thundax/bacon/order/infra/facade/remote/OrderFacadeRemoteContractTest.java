package com.github.thundax.bacon.order.infra.facade.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OrderFacadeRemoteContractTest {

    private static final String BASE_URL = "http://order.test/api";
    private static final String PROVIDER_TOKEN = "order-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallOrderReadProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/order/queries/detail?orderNo=ORD-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "orderNo": "ORD-1",
                          "userId": 2001,
                          "orderStatus": "PAID",
                          "payStatus": "PAID",
                          "inventoryStatus": "RESERVED",
                          "paymentNo": "PAY-1",
                          "reservationNo": "RSV-1",
                          "currencyCode": "CNY",
                          "totalAmount": 20,
                          "payableAmount": 20,
                          "cancelReason": null,
                          "closeReason": null,
                          "createdAt": "2026-03-26T10:00:00Z",
                          "expiredAt": "2026-03-26T10:30:00Z",
                          "items": [
                            {
                              "skuId": 3001,
                              "skuName": "SKU-1",
                              "imageUrl": "https://cdn.example.com/sku.png",
                              "quantity": 2,
                              "salePrice": 10,
                              "lineAmount": 20
                            }
                          ],
                          "paymentSnapshot": "payment-ok",
                          "inventorySnapshot": "inventory-ok",
                          "paidAt": "2026-03-26T10:05:00Z",
                          "closedAt": null
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(request -> {
                    assertThat(request.getURI().getPath()).isEqualTo("/api/providers/order/queries/page");
                    assertThat(request.getURI().getRawQuery())
                            .contains("userId=2001")
                            .contains("orderNo=ORD-1")
                            .contains("orderStatus=CREATED")
                            .contains("payStatus=UNPAID")
                            .contains("inventoryStatus=UNRESERVED")
                            .contains("createdAtFrom=2026-03-26T10:00:00Z")
                            .contains("createdAtTo=2026-03-27T10:00:00Z")
                            .contains("pageNo=2")
                            .contains("pageSize=20");
                })
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "records": [
                            {
                              "orderNo": "ORD-1",
                              "userId": 2001,
                              "orderStatus": "CREATED",
                              "payStatus": "UNPAID",
                              "inventoryStatus": "UNRESERVED",
                              "paymentNo": null,
                              "reservationNo": null,
                              "currencyCode": "CNY",
                              "totalAmount": 10,
                              "payableAmount": 10,
                              "cancelReason": null,
                              "closeReason": null,
                              "createdAt": "2026-03-26T10:00:00Z",
                              "expiredAt": "2026-03-26T10:30:00Z"
                            }
                          ],
                          "total": 21,
                          "pageNo": 2,
                          "pageSize": 20
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        OrderReadFacadeRemoteImpl facade = newReadFacade();

        OrderDetailFacadeResponse detail = facade.getByOrderNo(new OrderDetailFacadeRequest("ORD-1"));
        OrderPageFacadeResponse page = facade.page(new OrderPageFacadeRequest(
                2001L,
                "ORD-1",
                "CREATED",
                "UNPAID",
                "UNRESERVED",
                Instant.parse("2026-03-26T10:00:00Z"),
                Instant.parse("2026-03-27T10:00:00Z"),
                2,
                20));

        assertThat(detail.getItems()).hasSize(1);
        assertThat(detail.getPaymentNo()).isEqualTo("PAY-1");
        assertThat(page.getRecords()).extracting(record -> record.getOrderNo()).containsExactly("ORD-1");
        assertThat(page.getTotal()).isEqualTo(21);
        assertThat(page.getPageNo()).isEqualTo(2);
        assertThat(page.getPageSize()).isEqualTo(20);
        server.verify();
    }

    @Test
    void shouldCallOrderCommandProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/order/commands/mark-paid"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL + "/providers/order/commands/mark-payment-failed"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL + "/providers/order/commands/close-expired"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        OrderCommandFacadeRemoteImpl facade = newCommandFacade();

        facade.markPaid(new OrderMarkPaidFacadeRequest(
                "ORD-1", "PAY-1", "WECHAT", BigDecimal.TEN, Instant.parse("2026-03-26T10:05:00Z")));
        facade.markPaymentFailed(new OrderMarkPaymentFailedFacadeRequest(
                "ORD-2", "PAY-2", "insufficient balance", "FAILED", Instant.parse("2026-03-26T10:06:00Z")));
        facade.closeExpiredOrder(new OrderCloseExpiredFacadeRequest("ORD-1", "expired"));

        server.verify();
    }

    private OrderReadFacadeRemoteImpl newReadFacade() {
        return new OrderReadFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private OrderCommandFacadeRemoteImpl newCommandFacade() {
        return new OrderCommandFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private RestClientFactory restClientFactory() {
        @SuppressWarnings("unchecked")
        ObjectProvider<RestClient.Builder> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable(Mockito.any())).thenReturn(restClientBuilder);
        return new RestClientFactory(provider);
    }
}
