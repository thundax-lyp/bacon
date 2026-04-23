package com.github.thundax.bacon.payment.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCloseResult;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateResult;
import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeLocalContractTest {

    @Mock
    private PaymentCreateApplicationService paymentCreateApplicationService;

    @Mock
    private PaymentCloseApplicationService paymentCloseApplicationService;

    @Mock
    private PaymentQueryApplicationService paymentQueryApplicationService;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldMapCommandFacadeToApplicationCommandsAndResults() {
        when(paymentCreateApplicationService.createPayment(argThat(command -> command != null
                        && command.orderNo().equals("ORD-1")
                        && command.userId().equals(2001L)
                        && command.amount().compareTo(BigDecimal.TEN) == 0
                        && command.channelCode().equals("MOCK")
                        && command.subject().equals("test-subject")
                        && command.expiredAt().equals(Instant.parse("2026-03-27T10:30:00Z")))))
                .thenReturn(new PaymentCreateResult(
                        "PAY-1",
                        "ORD-1",
                        "MOCK",
                        "PAYING",
                        "mock://pay/PAY-1",
                        Instant.parse("2026-03-27T10:30:00Z"),
                        null));
        when(paymentCloseApplicationService.closePayment(argThat(command -> command != null
                        && command.paymentNo().equals("PAY-1")
                        && command.reason().equals("SYSTEM_CANCELLED"))))
                .thenReturn(new PaymentCloseResult("PAY-1", "ORD-1", "CLOSED", "SUCCESS", "SYSTEM_CANCELLED", null));
        PaymentCommandFacadeLocalImpl facade =
                new PaymentCommandFacadeLocalImpl(paymentCreateApplicationService, paymentCloseApplicationService);

        PaymentCreateFacadeResponse created = facade.createPayment(new PaymentCreateFacadeRequest(
                "ORD-1", 2001L, BigDecimal.TEN, "MOCK", "test-subject", Instant.parse("2026-03-27T10:30:00Z")));
        PaymentCloseFacadeResponse closed = facade.closePayment(new PaymentCloseFacadeRequest("PAY-1", "SYSTEM_CANCELLED"));

        assertThat(created.getPaymentNo()).isEqualTo("PAY-1");
        assertThat(created.getPayPayload()).isEqualTo("mock://pay/PAY-1");
        assertThat(closed.getCloseResult()).isEqualTo("SUCCESS");
        verify(paymentCloseApplicationService)
                .closePayment(argThat(command -> command.paymentNo().equals("PAY-1")));
    }

    @Test
    void shouldMapReadFacadeToApplicationQueriesAndDetailResponse() {
        when(paymentQueryApplicationService.getByPaymentNo(argThat(
                        query -> query != null && query.paymentNo().equals("PAY-1"))))
                .thenReturn(detailDto());
        when(paymentQueryApplicationService.getByOrderNo(argThat(
                        query -> query != null && query.orderNo().equals("ORD-1"))))
                .thenReturn(detailDto());
        PaymentReadFacadeLocalImpl facade = new PaymentReadFacadeLocalImpl(paymentQueryApplicationService);

        PaymentDetailFacadeResponse byPaymentNo = facade.getByPaymentNo(new PaymentGetByPaymentNoFacadeRequest("PAY-1"));
        PaymentDetailFacadeResponse byOrderNo = facade.getByOrderNo(new PaymentGetByOrderNoFacadeRequest("ORD-1"));

        assertThat(byPaymentNo.getOrderNo()).isEqualTo("ORD-1");
        assertThat(byPaymentNo.getPaymentStatus()).isEqualTo("PAID");
        assertThat(byPaymentNo.getCallbackSummary()).isEqualTo("{\"tradeStatus\":\"SUCCESS\"}");
        assertThat(byOrderNo.getPaymentNo()).isEqualTo("PAY-1");
    }

    private PaymentDetailDTO detailDto() {
        return new PaymentDetailDTO(
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
    }
}
