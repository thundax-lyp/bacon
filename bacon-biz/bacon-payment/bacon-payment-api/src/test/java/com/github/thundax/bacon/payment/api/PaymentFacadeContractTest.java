package com.github.thundax.bacon.payment.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PaymentFacadeContractTest {

    @Test
    void shouldKeepReadRequestContracts() throws Exception {
        assertField(PaymentGetByPaymentNoFacadeRequest.class, "paymentNo", String.class);
        assertField(PaymentGetByOrderNoFacadeRequest.class, "orderNo", String.class);

        assertThat(new PaymentGetByPaymentNoFacadeRequest("PAY-1").getPaymentNo()).isEqualTo("PAY-1");
        assertThat(new PaymentGetByOrderNoFacadeRequest("ORD-1").getOrderNo()).isEqualTo("ORD-1");
    }

    @Test
    void shouldKeepCommandRequestContracts() throws Exception {
        assertField(PaymentCreateFacadeRequest.class, "orderNo", String.class);
        assertField(PaymentCreateFacadeRequest.class, "userId", Long.class);
        assertField(PaymentCreateFacadeRequest.class, "amount", BigDecimal.class);
        assertField(PaymentCreateFacadeRequest.class, "channelCode", String.class);
        assertField(PaymentCreateFacadeRequest.class, "subject", String.class);
        assertField(PaymentCreateFacadeRequest.class, "expiredAt", Instant.class);
        assertField(PaymentCloseFacadeRequest.class, "paymentNo", String.class);
        assertField(PaymentCloseFacadeRequest.class, "reason", String.class);

        PaymentCreateFacadeRequest createRequest = new PaymentCreateFacadeRequest(
                "ORD-1", 2001L, BigDecimal.TEN, "MOCK", "test-subject", Instant.parse("2026-03-27T10:30:00Z"));

        assertThat(createRequest.getOrderNo()).isEqualTo("ORD-1");
        assertThat(createRequest.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(new PaymentCloseFacadeRequest("PAY-1", "SYSTEM_CANCELLED").getReason())
                .isEqualTo("SYSTEM_CANCELLED");
    }

    @Test
    void shouldKeepCreateAndCloseResponseContracts() throws Exception {
        assertField(PaymentCreateFacadeResponse.class, "paymentNo", String.class);
        assertField(PaymentCreateFacadeResponse.class, "orderNo", String.class);
        assertField(PaymentCreateFacadeResponse.class, "channelCode", String.class);
        assertField(PaymentCreateFacadeResponse.class, "paymentStatus", String.class);
        assertField(PaymentCreateFacadeResponse.class, "payPayload", String.class);
        assertField(PaymentCreateFacadeResponse.class, "expiredAt", Instant.class);
        assertField(PaymentCreateFacadeResponse.class, "failureReason", String.class);
        assertField(PaymentCloseFacadeResponse.class, "paymentNo", String.class);
        assertField(PaymentCloseFacadeResponse.class, "orderNo", String.class);
        assertField(PaymentCloseFacadeResponse.class, "paymentStatus", String.class);
        assertField(PaymentCloseFacadeResponse.class, "closeResult", String.class);
        assertField(PaymentCloseFacadeResponse.class, "closeReason", String.class);
        assertField(PaymentCloseFacadeResponse.class, "failureReason", String.class);

        PaymentCreateFacadeResponse createResponse = new PaymentCreateFacadeResponse(
                "PAY-1", "ORD-1", "MOCK", "PAYING", "mock://pay/PAY-1", Instant.parse("2026-03-27T10:30:00Z"), null);
        PaymentCloseFacadeResponse closeResponse =
                new PaymentCloseFacadeResponse("PAY-1", "ORD-1", "CLOSED", "SUCCESS", "SYSTEM_CANCELLED", null);

        assertThat(createResponse.getPayPayload()).isEqualTo("mock://pay/PAY-1");
        assertThat(closeResponse.getCloseResult()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldKeepDetailResponseContract() throws Exception {
        assertField(PaymentDetailFacadeResponse.class, "paymentNo", String.class);
        assertField(PaymentDetailFacadeResponse.class, "orderNo", String.class);
        assertField(PaymentDetailFacadeResponse.class, "userId", Long.class);
        assertField(PaymentDetailFacadeResponse.class, "channelCode", String.class);
        assertField(PaymentDetailFacadeResponse.class, "paymentStatus", String.class);
        assertField(PaymentDetailFacadeResponse.class, "amount", BigDecimal.class);
        assertField(PaymentDetailFacadeResponse.class, "paidAmount", BigDecimal.class);
        assertField(PaymentDetailFacadeResponse.class, "createdAt", Instant.class);
        assertField(PaymentDetailFacadeResponse.class, "expiredAt", Instant.class);
        assertField(PaymentDetailFacadeResponse.class, "paidAt", Instant.class);
        assertField(PaymentDetailFacadeResponse.class, "subject", String.class);
        assertField(PaymentDetailFacadeResponse.class, "closedAt", Instant.class);
        assertField(PaymentDetailFacadeResponse.class, "channelTransactionNo", String.class);
        assertField(PaymentDetailFacadeResponse.class, "channelStatus", String.class);
        assertField(PaymentDetailFacadeResponse.class, "callbackSummary", String.class);

        PaymentDetailFacadeResponse response = new PaymentDetailFacadeResponse(
                "PAY-1",
                "ORD-1",
                2001L,
                "MOCK",
                "PAID",
                BigDecimal.TEN,
                BigDecimal.TEN,
                Instant.parse("2026-03-27T10:00:00Z"),
                Instant.parse("2026-03-27T10:30:00Z"),
                Instant.parse("2026-03-27T10:01:00Z"),
                "test-subject",
                null,
                "TXN-1",
                "SUCCESS",
                "{\"tradeStatus\":\"SUCCESS\"}");

        assertThat(response.getPaymentNo()).isEqualTo("PAY-1");
        assertThat(response.getPaidAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(response.getChannelTransactionNo()).isEqualTo("TXN-1");
    }

    private void assertField(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertThat(field.getType()).isEqualTo(fieldType);
    }
}
