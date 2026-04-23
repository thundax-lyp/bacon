package com.github.thundax.bacon.payment.infra.facade.remote;

import static org.assertj.core.api.Assertions.assertThat;
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
        restClientBuilder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("X-Bacon-Provider-Token", PROVIDER_TOKEN);
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallPaymentReadProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/payment/queries/detail?paymentNo=PAY-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(detailJson(), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/payment/queries/by-order?orderNo=ORD-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(detailJson(), MediaType.APPLICATION_JSON));
        PaymentReadFacadeRemoteImpl facade = new PaymentReadFacadeRemoteImpl(restClientBuilder.build());

        PaymentDetailFacadeResponse byPaymentNo = facade.getByPaymentNo(new PaymentGetByPaymentNoFacadeRequest("PAY-1"));
        PaymentDetailFacadeResponse byOrderNo = facade.getByOrderNo(new PaymentGetByOrderNoFacadeRequest("ORD-1"));

        assertThat(byPaymentNo.getOrderNo()).isEqualTo("ORD-1");
        assertThat(byPaymentNo.getCallbackSummary()).isEqualTo("{\"tradeStatus\":\"SUCCESS\"}");
        assertThat(byOrderNo.getPaymentNo()).isEqualTo("PAY-1");
        server.verify();
    }

    @Test
    void shouldCallPaymentCommandProviderPaths() {
        server.expect(requestTo(BASE_URL
                        + "/providers/payment/commands/create?orderNo=ORD-1&userId=2001"
                        + "&amount=10&channelCode=MOCK&subject=test-subject&expiredAt=2026-03-27T10%3A30%3A00Z"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentNo": "PAY-1",
                          "orderNo": "ORD-1",
                          "channelCode": "MOCK",
                          "paymentStatus": "PAYING",
                          "payPayload": "mock://pay/PAY-1",
                          "expiredAt": "2026-03-27T10:30:00Z",
                          "failureReason": null
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/payment/commands/close?paymentNo=PAY-1&reason=SYSTEM_CANCELLED"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentNo": "PAY-1",
                          "orderNo": "ORD-1",
                          "paymentStatus": "CLOSED",
                          "closeResult": "SUCCESS",
                          "closeReason": "SYSTEM_CANCELLED",
                          "failureReason": null
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        PaymentCommandFacadeRemoteImpl facade = new PaymentCommandFacadeRemoteImpl(restClientBuilder.build());

        PaymentCreateFacadeResponse created = facade.createPayment(new PaymentCreateFacadeRequest(
                "ORD-1", 2001L, BigDecimal.TEN, "MOCK", "test-subject", Instant.parse("2026-03-27T10:30:00Z")));
        PaymentCloseFacadeResponse closed = facade.closePayment(new PaymentCloseFacadeRequest("PAY-1", "SYSTEM_CANCELLED"));

        assertThat(created.getPaymentNo()).isEqualTo("PAY-1");
        assertThat(closed.getCloseResult()).isEqualTo("SUCCESS");
        server.verify();
    }

    private String detailJson() {
        return """
                {
                  "paymentNo": "PAY-1",
                  "orderNo": "ORD-1",
                  "userId": 2001,
                  "channelCode": "MOCK",
                  "paymentStatus": "PAID",
                  "amount": 10,
                  "paidAmount": 10,
                  "createdAt": "2026-03-27T10:00:00Z",
                  "expiredAt": "2026-03-27T10:30:00Z",
                  "paidAt": "2026-03-27T10:01:00Z",
                  "subject": "test-subject",
                  "closedAt": null,
                  "channelTransactionNo": "TXN-1",
                  "channelStatus": "SUCCESS",
                  "callbackSummary": "{\\"tradeStatus\\":\\"SUCCESS\\"}"
                }
                """;
    }
}
