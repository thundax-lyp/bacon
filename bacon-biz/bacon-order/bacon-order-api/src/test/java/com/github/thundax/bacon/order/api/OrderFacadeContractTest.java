package com.github.thundax.bacon.order.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderItemFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderSummaryFacadeResponse;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderFacadeContractTest {

    @Test
    void shouldKeepReadRequestContracts() throws Exception {
        assertField(OrderDetailFacadeRequest.class, "orderNo", String.class);
        assertField(OrderPageFacadeRequest.class, "userId", Long.class);
        assertField(OrderPageFacadeRequest.class, "orderNo", String.class);
        assertField(OrderPageFacadeRequest.class, "orderStatus", String.class);
        assertField(OrderPageFacadeRequest.class, "payStatus", String.class);
        assertField(OrderPageFacadeRequest.class, "inventoryStatus", String.class);
        assertField(OrderPageFacadeRequest.class, "createdAtFrom", Instant.class);
        assertField(OrderPageFacadeRequest.class, "createdAtTo", Instant.class);
        assertField(OrderPageFacadeRequest.class, "pageNo", Integer.class);
        assertField(OrderPageFacadeRequest.class, "pageSize", Integer.class);

        OrderPageFacadeRequest request = new OrderPageFacadeRequest(
                2001L,
                "ORD-1",
                "CREATED",
                "UNPAID",
                "UNRESERVED",
                Instant.parse("2026-03-26T10:00:00Z"),
                Instant.parse("2026-03-27T10:00:00Z"),
                2,
                20);

        assertThat(new OrderDetailFacadeRequest("ORD-1").getOrderNo()).isEqualTo("ORD-1");
        assertThat(request.getUserId()).isEqualTo(2001L);
        assertThat(request.getOrderStatus()).isEqualTo("CREATED");
        assertThat(request.getPageNo()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(20);
    }

    @Test
    void shouldKeepCommandRequestContracts() throws Exception {
        assertField(OrderMarkPaidFacadeRequest.class, "orderNo", String.class);
        assertField(OrderMarkPaidFacadeRequest.class, "paymentNo", String.class);
        assertField(OrderMarkPaidFacadeRequest.class, "channelCode", String.class);
        assertField(OrderMarkPaidFacadeRequest.class, "paidAmount", BigDecimal.class);
        assertField(OrderMarkPaidFacadeRequest.class, "paidTime", Instant.class);
        assertField(OrderMarkPaymentFailedFacadeRequest.class, "orderNo", String.class);
        assertField(OrderMarkPaymentFailedFacadeRequest.class, "paymentNo", String.class);
        assertField(OrderMarkPaymentFailedFacadeRequest.class, "reason", String.class);
        assertField(OrderMarkPaymentFailedFacadeRequest.class, "channelStatus", String.class);
        assertField(OrderMarkPaymentFailedFacadeRequest.class, "failedTime", Instant.class);
        assertField(OrderCloseExpiredFacadeRequest.class, "orderNo", String.class);
        assertField(OrderCloseExpiredFacadeRequest.class, "reason", String.class);

        OrderMarkPaidFacadeRequest paidRequest = new OrderMarkPaidFacadeRequest(
                "ORD-1", "PAY-1", "WECHAT", BigDecimal.TEN, Instant.parse("2026-03-26T10:05:00Z"));
        OrderMarkPaymentFailedFacadeRequest failedRequest = new OrderMarkPaymentFailedFacadeRequest(
                "ORD-1", "PAY-1", "insufficient balance", "FAILED", Instant.parse("2026-03-26T10:06:00Z"));

        assertThat(paidRequest.getPaymentNo()).isEqualTo("PAY-1");
        assertThat(paidRequest.getPaidAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(failedRequest.getReason()).isEqualTo("insufficient balance");
        assertThat(new OrderCloseExpiredFacadeRequest("ORD-1", "expired").getReason())
                .isEqualTo("expired");
    }

    @Test
    void shouldKeepDetailResponseContract() throws Exception {
        assertDetailFields(OrderDetailFacadeResponse.class);
        assertField(OrderDetailFacadeResponse.class, "items", List.class);
        assertField(OrderDetailFacadeResponse.class, "paymentSnapshot", String.class);
        assertField(OrderDetailFacadeResponse.class, "inventorySnapshot", String.class);
        assertField(OrderDetailFacadeResponse.class, "paidAt", Instant.class);
        assertField(OrderDetailFacadeResponse.class, "closedAt", Instant.class);
        assertField(OrderItemFacadeResponse.class, "skuId", Long.class);
        assertField(OrderItemFacadeResponse.class, "skuName", String.class);
        assertField(OrderItemFacadeResponse.class, "imageUrl", String.class);
        assertField(OrderItemFacadeResponse.class, "quantity", Integer.class);
        assertField(OrderItemFacadeResponse.class, "salePrice", BigDecimal.class);
        assertField(OrderItemFacadeResponse.class, "lineAmount", BigDecimal.class);

        OrderItemFacadeResponse item = new OrderItemFacadeResponse(
                3001L, "SKU-1", "https://cdn.example.com/sku.png", 2, BigDecimal.TEN, BigDecimal.valueOf(20));
        OrderDetailFacadeResponse response = new OrderDetailFacadeResponse(
                "ORD-1",
                2001L,
                "PAID",
                "PAID",
                "RESERVED",
                "PAY-1",
                "RSV-1",
                "CNY",
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(20),
                null,
                null,
                Instant.parse("2026-03-26T10:00:00Z"),
                Instant.parse("2026-03-26T10:30:00Z"),
                List.of(item),
                "payment-ok",
                "inventory-ok",
                Instant.parse("2026-03-26T10:05:00Z"),
                null);

        assertThat(response.getOrderNo()).isEqualTo("ORD-1");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getSkuId()).isEqualTo(3001L);
        assertThat(response.getPaidAt()).isEqualTo(Instant.parse("2026-03-26T10:05:00Z"));
    }

    @Test
    void shouldKeepPageResponseContract() throws Exception {
        assertDetailFields(OrderSummaryFacadeResponse.class);
        assertField(OrderPageFacadeResponse.class, "records", List.class);
        assertField(OrderPageFacadeResponse.class, "total", long.class);
        assertField(OrderPageFacadeResponse.class, "pageNo", int.class);
        assertField(OrderPageFacadeResponse.class, "pageSize", int.class);

        OrderSummaryFacadeResponse summary = new OrderSummaryFacadeResponse(
                "ORD-1",
                2001L,
                "CREATED",
                "UNPAID",
                "UNRESERVED",
                null,
                null,
                "CNY",
                BigDecimal.TEN,
                BigDecimal.TEN,
                null,
                null,
                Instant.parse("2026-03-26T10:00:00Z"),
                Instant.parse("2026-03-26T10:30:00Z"));
        OrderPageFacadeResponse response = new OrderPageFacadeResponse(List.of(summary), 21, 2, 20);

        assertThat(response.getRecords()).extracting(OrderSummaryFacadeResponse::getOrderNo).containsExactly("ORD-1");
        assertThat(response.getTotal()).isEqualTo(21);
        assertThat(response.getPageNo()).isEqualTo(2);
        assertThat(response.getPageSize()).isEqualTo(20);
    }

    private void assertDetailFields(Class<?> type) throws Exception {
        assertField(type, "orderNo", String.class);
        assertField(type, "userId", Long.class);
        assertField(type, "orderStatus", String.class);
        assertField(type, "payStatus", String.class);
        assertField(type, "inventoryStatus", String.class);
        assertField(type, "paymentNo", String.class);
        assertField(type, "reservationNo", String.class);
        assertField(type, "currencyCode", String.class);
        assertField(type, "totalAmount", BigDecimal.class);
        assertField(type, "payableAmount", BigDecimal.class);
        assertField(type, "cancelReason", String.class);
        assertField(type, "closeReason", String.class);
        assertField(type, "createdAt", Instant.class);
        assertField(type, "expiredAt", Instant.class);
    }

    private void assertField(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertThat(field.getType()).isEqualTo(fieldType);
    }
}
