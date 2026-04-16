package com.github.thundax.bacon.payment.infra.facade.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PaymentFacadeRemoteContractTest {

    private static final String BASE_URL = "http://payment.test/api";
    private static final String PROVIDER_TOKEN = "payment-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder().baseUrl(BASE_URL).defaultHeader("X-Bacon-Provider-Token", PROVIDER_TOKEN);
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallCreatePaymentProviderPath() {
        server.expect(requestTo(BASE_URL
                        + "/providers/payment/create?orderNo=ORD-10001&userId=2001"
                        + "&amount=88.80&channelCode=MOCK&subject=test-subject&expiredAt=2026-03-27T10%3A30%3A00Z"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentNo": "PAY-10001",
                          "orderNo": "ORD-10001",
                          "channelCode": "MOCK",
                          "paymentStatus": "PAYING",
                          "payPayload": "mock://pay/PAY-10001",
                          "expiredAt": "2026-03-27T10:30:00Z",
                          "failureReason": null
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentCommandFacadeRemoteImpl facade = new PaymentCommandFacadeRemoteImpl(restClientBuilder.build());
        PaymentCreateFacadeResponse response = facade.createPayment(new PaymentCreateFacadeRequest(
                "ORD-10001", 2001L, new BigDecimal("88.80"), "MOCK", "test-subject", Instant.parse("2026-03-27T10:30:00Z")));

        assertEquals("PAY-10001", response.getPaymentNo());
        assertEquals("PAYING", response.getPaymentStatus());
        server.verify();
    }

    @Test
    void shouldCallClosePaymentProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/payment/close?paymentNo=PAY-10001&reason=SYSTEM_CANCELLED"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentNo": "PAY-10001",
                          "orderNo": "ORD-10001",
                          "paymentStatus": "CLOSED",
                          "closeResult": "SUCCESS",
                          "closeReason": "SYSTEM_CANCELLED",
                          "failureReason": null
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentCommandFacadeRemoteImpl facade = new PaymentCommandFacadeRemoteImpl(restClientBuilder.build());
        PaymentCloseFacadeResponse response = facade.closePayment(new PaymentCloseFacadeRequest("PAY-10001", "SYSTEM_CANCELLED"));

        assertEquals("SUCCESS", response.getCloseResult());
        assertEquals("SYSTEM_CANCELLED", response.getCloseReason());
        server.verify();
    }

    @Test
    void shouldCallGetByPaymentNoProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/payment/PAY-10001"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentNo": "PAY-10001",
                          "orderNo": "ORD-10001",
                          "userId": 2001,
                          "channelCode": "MOCK",
                          "paymentStatus": "PAYING",
                          "amount": 88.80,
                          "paidAmount": null,
                          "createdAt": "2026-03-27T10:00:00Z",
                          "expiredAt": "2026-03-27T10:30:00Z",
                          "paidAt": null,
                          "subject": "test-subject",
                          "closedAt": null,
                          "channelTransactionNo": null,
                          "channelStatus": null,
                          "callbackSummary": null
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentReadFacadeRemoteImpl facade = new PaymentReadFacadeRemoteImpl(restClientBuilder.build());
        PaymentDetailFacadeResponse response = facade.getByPaymentNo(new PaymentGetByPaymentNoFacadeRequest("PAY-10001"));

        assertEquals("ORD-10001", response.getOrderNo());
        assertEquals("PAYING", response.getPaymentStatus());
        server.verify();
    }

    @Test
    void shouldCallGetByOrderNoProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/payment?orderNo=ORD-10001"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentNo": "PAY-10001",
                          "orderNo": "ORD-10001",
                          "userId": 2001,
                          "channelCode": "MOCK",
                          "paymentStatus": "PAYING",
                          "amount": 88.80,
                          "paidAmount": null,
                          "createdAt": "2026-03-27T10:00:00Z",
                          "expiredAt": "2026-03-27T10:30:00Z",
                          "paidAt": null,
                          "subject": "test-subject",
                          "closedAt": null,
                          "channelTransactionNo": null,
                          "channelStatus": null,
                          "callbackSummary": null
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        PaymentReadFacadeRemoteImpl facade = new PaymentReadFacadeRemoteImpl(restClientBuilder.build());
        PaymentDetailFacadeResponse response = facade.getByOrderNo(new PaymentGetByOrderNoFacadeRequest("ORD-10001"));

        assertEquals("PAY-10001", response.getPaymentNo());
        assertEquals("ORD-10001", response.getOrderNo());
        server.verify();
    }
}
