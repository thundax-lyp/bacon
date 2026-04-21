package com.github.thundax.bacon.payment.interfaces.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.payment.application.command.PaymentCloseCommand;
import com.github.thundax.bacon.payment.application.command.PaymentCreateCommand;
import com.github.thundax.bacon.payment.application.command.PaymentCloseResult;
import com.github.thundax.bacon.payment.application.command.PaymentCreateResult;
import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.query.PaymentGetByOrderNoQuery;
import com.github.thundax.bacon.payment.application.query.PaymentGetByPaymentNoQuery;
import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PaymentFacadeLocalImplTest {

    @BeforeEach
    void setUp() {
        BaconContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void commandFacadeShouldDelegateCreatePaymentWithFacadeRequest() {
        PaymentCreateApplicationService createService = Mockito.mock(PaymentCreateApplicationService.class);
        PaymentCloseApplicationService closeService = Mockito.mock(PaymentCloseApplicationService.class);
        PaymentCommandFacadeLocalImpl facade = new PaymentCommandFacadeLocalImpl(createService, closeService);
        PaymentCreateFacadeRequest request = new PaymentCreateFacadeRequest(
                "ORD-10001", 2001L, new BigDecimal("88.80"), "MOCK", "test-subject", Instant.parse("2026-03-27T10:30:00Z"));
        when(createService.createPayment(new PaymentCreateCommand(
                        request.getOrderNo(),
                        request.getUserId(),
                        request.getAmount(),
                        request.getChannelCode(),
                        request.getSubject(),
                        request.getExpiredAt())))
                .thenReturn(new PaymentCreateResult(
                        "PAY-10001", "ORD-10001", "MOCK", "PAYING", "mock://pay/PAY-10001", request.getExpiredAt(), null));
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        PaymentCreateFacadeResponse response = facade.createPayment(request);

        assertEquals("PAY-10001", response.getPaymentNo());
        assertEquals("ORD-10001", response.getOrderNo());
        verify(createService).createPayment(new PaymentCreateCommand(
                request.getOrderNo(),
                request.getUserId(),
                request.getAmount(),
                request.getChannelCode(),
                request.getSubject(),
                request.getExpiredAt()));
    }

    @Test
    void commandFacadeShouldRequireTenantBeforeClosePayment() {
        PaymentCreateApplicationService createService = Mockito.mock(PaymentCreateApplicationService.class);
        PaymentCloseApplicationService closeService = Mockito.mock(PaymentCloseApplicationService.class);
        PaymentCommandFacadeLocalImpl facade = new PaymentCommandFacadeLocalImpl(createService, closeService);
        BaconContextHolder.clear();

        assertThrows(IllegalStateException.class, () -> facade.closePayment(new PaymentCloseFacadeRequest("PAY-10001", "SYSTEM_CANCELLED")));
        verifyNoInteractions(closeService);
    }

    @Test
    void commandFacadeShouldDelegateClosePaymentWithFacadeRequest() {
        PaymentCreateApplicationService createService = Mockito.mock(PaymentCreateApplicationService.class);
        PaymentCloseApplicationService closeService = Mockito.mock(PaymentCloseApplicationService.class);
        PaymentCommandFacadeLocalImpl facade = new PaymentCommandFacadeLocalImpl(createService, closeService);
        PaymentCloseFacadeRequest request = new PaymentCloseFacadeRequest("PAY-10001", "SYSTEM_CANCELLED");
        when(closeService.closePayment(new PaymentCloseCommand(request.getPaymentNo(), request.getReason())))
                .thenReturn(new PaymentCloseResult("PAY-10001", "ORD-10001", "CLOSED", "SUCCESS", "SYSTEM_CANCELLED", null));
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        PaymentCloseFacadeResponse response = facade.closePayment(request);

        assertEquals("SUCCESS", response.getCloseResult());
        assertEquals("SYSTEM_CANCELLED", response.getCloseReason());
        verify(closeService).closePayment(new PaymentCloseCommand(request.getPaymentNo(), request.getReason()));
    }

    @Test
    void readFacadeShouldDelegateGetByPaymentNoWithFacadeRequest() {
        PaymentQueryApplicationService queryService = Mockito.mock(PaymentQueryApplicationService.class);
        PaymentReadFacadeLocalImpl facade = new PaymentReadFacadeLocalImpl(queryService);
        PaymentGetByPaymentNoFacadeRequest request = new PaymentGetByPaymentNoFacadeRequest("PAY-10001");
        when(queryService.getByPaymentNo(new PaymentGetByPaymentNoQuery(request.getPaymentNo())))
                .thenReturn(new PaymentDetailDTO(
                        "PAY-10001",
                        "ORD-10001",
                        2001L,
                        "MOCK",
                        "PAYING",
                        new BigDecimal("88.80"),
                        null,
                        Instant.parse("2026-03-27T10:00:00Z"),
                        Instant.parse("2026-03-27T10:30:00Z"),
                        null,
                        "subject",
                        null,
                        null,
                        null,
                        null));
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        PaymentDetailFacadeResponse response = facade.getByPaymentNo(request);

        assertEquals("ORD-10001", response.getOrderNo());
        verify(queryService).getByPaymentNo(new PaymentGetByPaymentNoQuery(request.getPaymentNo()));
    }

    @Test
    void readFacadeShouldDelegateGetByOrderNoWithFacadeRequest() {
        PaymentQueryApplicationService queryService = Mockito.mock(PaymentQueryApplicationService.class);
        PaymentReadFacadeLocalImpl facade = new PaymentReadFacadeLocalImpl(queryService);
        PaymentGetByOrderNoFacadeRequest request = new PaymentGetByOrderNoFacadeRequest("ORD-10001");
        when(queryService.getByOrderNo(new PaymentGetByOrderNoQuery(request.getOrderNo())))
                .thenReturn(new PaymentDetailDTO(
                        "PAY-10001",
                        "ORD-10001",
                        2001L,
                        "MOCK",
                        "PAYING",
                        new BigDecimal("88.80"),
                        null,
                        Instant.parse("2026-03-27T10:00:00Z"),
                        Instant.parse("2026-03-27T10:30:00Z"),
                        null,
                        "subject",
                        null,
                        null,
                        null,
                        null));
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        PaymentDetailFacadeResponse response = facade.getByOrderNo(request);

        assertEquals("PAY-10001", response.getPaymentNo());
        verify(queryService).getByOrderNo(new PaymentGetByOrderNoQuery(request.getOrderNo()));
    }
}
