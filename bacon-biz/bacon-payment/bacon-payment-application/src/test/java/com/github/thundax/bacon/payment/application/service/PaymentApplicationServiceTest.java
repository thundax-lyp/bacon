package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.support.PaymentAuditLogSupport;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
class PaymentApplicationServiceTest {

    @Test
    void createPaymentShouldGeneratePaymentNoInsideModule() {
        TestPaymentRepository repository = new TestPaymentRepository();
        PaymentApplicationService service = new PaymentApplicationService(repository, new PaymentAuditLogSupport(repository),
                () -> "PAY-20001");

        PaymentCreateResultDTO result = service.createPayment(1001L, "ORD-10001", 2001L, BigDecimal.TEN,
                "MOCK", "test", Instant.now().plusSeconds(1800));

        assertEquals("PAY-20001", result.getPaymentNo());
        assertEquals("ORD-10001", result.getOrderNo());
        assertEquals(PaymentOrder.STATUS_PAYING, result.getPaymentStatus());
        assertEquals(1, repository.findAuditLogsByPaymentNo(1001L, "PAY-20001").size());
    }

    @Test
    void createPaymentShouldBeIdempotentByOrderNo() {
        TestPaymentRepository repository = new TestPaymentRepository();
        PaymentApplicationService service = new PaymentApplicationService(repository, new PaymentAuditLogSupport(repository),
                new SequencePaymentNoGenerator());

        PaymentCreateResultDTO first = service.createPayment(1001L, "ORD-10002", 2001L, BigDecimal.ONE,
                "MOCK", "test", Instant.now().plusSeconds(1800));
        PaymentCreateResultDTO second = service.createPayment(1001L, "ORD-10002", 2001L, BigDecimal.ONE,
                "MOCK", "test", Instant.now().plusSeconds(1800));

        assertEquals(first.getPaymentNo(), second.getPaymentNo());
        assertEquals(1, repository.findAuditLogsByPaymentNo(1001L, first.getPaymentNo()).size());
    }

    @Test
    void callbackAndCloseShouldRespectStateRules() {
        TestPaymentRepository repository = new TestPaymentRepository();
        StubOrderCommandFacade orderCommandFacade = new StubOrderCommandFacade();
        PaymentApplicationService createService = new PaymentApplicationService(repository, new PaymentAuditLogSupport(repository),
                () -> "PAY-20003");
        PaymentCallbackApplicationService callbackService = new PaymentCallbackApplicationService(repository, repository,
                new PaymentAuditLogSupport(repository), orderCommandFacade);
        PaymentQueryApplicationService queryService = new PaymentQueryApplicationService(repository, repository);
        PaymentCloseApplicationService closeService = new PaymentCloseApplicationService(repository,
                new PaymentAuditLogSupport(repository));

        PaymentCreateResultDTO created = createService.createPayment(1001L, "ORD-10003", 2003L, new BigDecimal("18.80"),
                "MOCK", "callback", Instant.now().plusSeconds(1800));
        callbackService.callbackPaid("MOCK", 1001L, created.getPaymentNo(), "TXN-1", "SUCCESS",
                "{\"tradeStatus\":\"SUCCESS\"}");
        callbackService.callbackFailed("MOCK", 1001L, created.getPaymentNo(), "FAILED",
                "{\"tradeStatus\":\"FAILED\"}", "CHANNEL_FAIL");
        PaymentDetailDTO detail = queryService.getByPaymentNo(1001L, created.getPaymentNo());

        assertEquals(PaymentOrder.STATUS_PAID, detail.getPaymentStatus());
        assertEquals("TXN-1", detail.getChannelTransactionNo());
        assertEquals("SUCCESS", detail.getChannelStatus());
        assertEquals(1, orderCommandFacade.markPaidCount);
        assertEquals(0, orderCommandFacade.markFailedCount);

        assertEquals("FAILED", closeService.closePayment(1001L, created.getPaymentNo(), "USER_CANCELLED").getCloseResult());
    }

    @Test
    void duplicateOrIgnoredCallbacksShouldStillWriteAuditLog() {
        TestPaymentRepository repository = new TestPaymentRepository();
        StubOrderCommandFacade orderCommandFacade = new StubOrderCommandFacade();
        PaymentApplicationService createService = new PaymentApplicationService(repository, new PaymentAuditLogSupport(repository),
                () -> "PAY-20007");
        PaymentCallbackApplicationService callbackService = new PaymentCallbackApplicationService(repository, repository,
                new PaymentAuditLogSupport(repository), orderCommandFacade);

        PaymentCreateResultDTO created = createService.createPayment(1001L, "ORD-10007", 2007L, new BigDecimal("20.00"),
                "MOCK", "duplicate-callback", Instant.now().plusSeconds(1800));
        callbackService.callbackPaid("MOCK", 1001L, created.getPaymentNo(), "TXN-20007", "SUCCESS",
                "{\"tradeStatus\":\"SUCCESS\"}");
        callbackService.callbackPaid("MOCK", 1001L, created.getPaymentNo(), "TXN-20007", "SUCCESS",
                "{\"tradeStatus\":\"SUCCESS\"}");
        callbackService.callbackFailed("MOCK", 1001L, created.getPaymentNo(), "FAILED",
                "{\"tradeStatus\":\"FAILED\"}", "CHANNEL_FAIL");

        List<PaymentAuditLog> auditLogs = repository.findAuditLogsByPaymentNo(1001L, created.getPaymentNo());

        assertEquals(4, auditLogs.size());
        assertEquals(PaymentAuditLog.ACTION_CREATE, auditLogs.get(0).getActionType());
        assertEquals(PaymentAuditLog.ACTION_CALLBACK_PAID, auditLogs.get(1).getActionType());
        assertEquals(PaymentAuditLog.ACTION_CALLBACK_PAID, auditLogs.get(2).getActionType());
        assertEquals(PaymentOrder.STATUS_PAID, auditLogs.get(2).getBeforeStatus());
        assertEquals(PaymentOrder.STATUS_PAID, auditLogs.get(2).getAfterStatus());
        assertEquals(PaymentAuditLog.ACTION_CALLBACK_FAILED, auditLogs.get(3).getActionType());
        assertEquals(PaymentOrder.STATUS_PAID, auditLogs.get(3).getBeforeStatus());
        assertEquals(PaymentOrder.STATUS_PAID, auditLogs.get(3).getAfterStatus());
        assertEquals(1, orderCommandFacade.markPaidCount);
        assertEquals(0, orderCommandFacade.markFailedCount);
    }

    @Test
    void closePaymentShouldBeIdempotentForClosedPayment() {
        TestPaymentRepository repository = new TestPaymentRepository();
        PaymentApplicationService createService = new PaymentApplicationService(repository, new PaymentAuditLogSupport(repository),
                () -> "PAY-20004");
        PaymentCloseApplicationService closeService = new PaymentCloseApplicationService(repository,
                new PaymentAuditLogSupport(repository));

        PaymentCreateResultDTO created = createService.createPayment(1001L, "ORD-10004", 2004L, new BigDecimal("28.00"),
                "MOCK", "close", Instant.now().plusSeconds(1800));
        PaymentCloseResultDTO first = closeService.closePayment(1001L, created.getPaymentNo(), "SYSTEM_CANCELLED");
        PaymentCloseResultDTO second = closeService.closePayment(1001L, created.getPaymentNo(), "SYSTEM_CANCELLED");

        assertEquals("SUCCESS", first.getCloseResult());
        assertEquals("SUCCESS", second.getCloseResult());
        assertEquals(PaymentOrder.STATUS_CLOSED, second.getPaymentStatus());
        assertEquals(2, repository.findAuditLogsByPaymentNo(1001L, created.getPaymentNo()).size());
    }

    @Test
    void createPaymentShouldNotRollbackWhenAuditWriteFails() {
        TestPaymentRepository repository = new TestPaymentRepository();
        repository.failAuditSave = true;
        PaymentApplicationService service = new PaymentApplicationService(repository, new PaymentAuditLogSupport(repository),
                () -> "PAY-20005");

        PaymentCreateResultDTO result = assertDoesNotThrow(() -> service.createPayment(1001L, "ORD-10005", 2005L,
                BigDecimal.ONE, "MOCK", "audit-fail", Instant.now().plusSeconds(1800)));

        assertEquals("PAY-20005", result.getPaymentNo());
        assertEquals(PaymentOrder.STATUS_PAYING, repository.findOrderByPaymentNo(1001L, "PAY-20005").orElseThrow().getPaymentStatus());
        assertEquals(0, repository.findAuditLogsByPaymentNo(1001L, "PAY-20005").size());
    }

    @Test
    void callbackPaidShouldNotRollbackWhenAuditWriteFails() {
        TestPaymentRepository repository = new TestPaymentRepository();
        StubOrderCommandFacade orderCommandFacade = new StubOrderCommandFacade();
        PaymentApplicationService createService = new PaymentApplicationService(repository, new PaymentAuditLogSupport(repository),
                () -> "PAY-20006");
        createService.createPayment(1001L, "ORD-10006", 2006L, new BigDecimal("18.00"),
                "MOCK", "audit-fail-callback", Instant.now().plusSeconds(1800));
        repository.failAuditSave = true;
        PaymentCallbackApplicationService callbackService = new PaymentCallbackApplicationService(repository, repository,
                new PaymentAuditLogSupport(repository), orderCommandFacade);

        assertDoesNotThrow(() -> callbackService.callbackPaid("MOCK", 1001L, "PAY-20006", "TXN-20006", "SUCCESS",
                "{\"tradeStatus\":\"SUCCESS\"}"));

        PaymentOrder paymentOrder = repository.findOrderByPaymentNo(1001L, "PAY-20006").orElseThrow();
        assertEquals(PaymentOrder.STATUS_PAID, paymentOrder.getPaymentStatus());
        assertEquals(1, orderCommandFacade.markPaidCount);
    }

    private static final class TestPaymentRepository implements PaymentOrderRepository, PaymentCallbackRecordRepository,
            PaymentAuditLogRepository {

        private final ConcurrentMap<String, PaymentOrder> paymentsByPaymentNo = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, PaymentOrder> paymentsByOrderNo = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, List<PaymentCallbackRecord>> callbacksByPaymentNo = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, PaymentCallbackRecord> callbacksByTxn = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, List<PaymentAuditLog>> auditLogsByPaymentNo = new ConcurrentHashMap<>();
        private boolean failAuditSave;

        @Override
        public PaymentOrder save(PaymentOrder paymentOrder) {
            paymentsByPaymentNo.put(paymentKey(paymentOrder.getTenantId(), paymentOrder.getPaymentNo()), paymentOrder);
            paymentsByOrderNo.put(orderKey(paymentOrder.getTenantId(), paymentOrder.getOrderNo()), paymentOrder);
            return paymentOrder;
        }

        @Override
        public Optional<PaymentOrder> findOrderByPaymentNo(Long tenantId, String paymentNo) {
            return Optional.ofNullable(paymentsByPaymentNo.get(paymentKey(tenantId, paymentNo)));
        }

        @Override
        public Optional<PaymentOrder> findOrderByOrderNo(Long tenantId, String orderNo) {
            return Optional.ofNullable(paymentsByOrderNo.get(orderKey(tenantId, orderNo)));
        }

        @Override
        public PaymentCallbackRecord save(PaymentCallbackRecord callbackRecord) {
            callbacksByPaymentNo.computeIfAbsent(paymentKey(callbackRecord.getTenantId(), callbackRecord.getPaymentNo()),
                    ignored -> new ArrayList<>()).add(callbackRecord);
            if (callbackRecord.getChannelTransactionNo() != null) {
                callbacksByTxn.put(txnKey(callbackRecord.getTenantId(), callbackRecord.getChannelCode(),
                        callbackRecord.getChannelTransactionNo()), callbackRecord);
            }
            return callbackRecord;
        }

        @Override
        public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(Long tenantId, String paymentNo) {
            return findCallbacksByPaymentNo(tenantId, paymentNo).stream()
                    .max(Comparator.comparing(PaymentCallbackRecord::getReceivedAt).thenComparing(PaymentCallbackRecord::getId));
        }

        @Override
        public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(Long tenantId, String channelCode,
                                                                                  String channelTransactionNo) {
            return Optional.ofNullable(callbacksByTxn.get(txnKey(tenantId, channelCode, channelTransactionNo)));
        }

        @Override
        public List<PaymentCallbackRecord> findCallbacksByPaymentNo(Long tenantId, String paymentNo) {
            return List.copyOf(callbacksByPaymentNo.getOrDefault(paymentKey(tenantId, paymentNo), List.of()));
        }

        @Override
        public void save(PaymentAuditLog auditLog) {
            if (failAuditSave) {
                throw new IllegalStateException("audit unavailable");
            }
            auditLogsByPaymentNo.computeIfAbsent(paymentKey(auditLog.getTenantId(), auditLog.getPaymentNo()),
                    ignored -> new ArrayList<>()).add(auditLog);
        }

        @Override
        public List<PaymentAuditLog> findAuditLogsByPaymentNo(Long tenantId, String paymentNo) {
            return List.copyOf(auditLogsByPaymentNo.getOrDefault(paymentKey(tenantId, paymentNo), List.of()));
        }

        private static String paymentKey(Long tenantId, String paymentNo) {
            return tenantId + ":" + paymentNo;
        }

        private static String orderKey(Long tenantId, String orderNo) {
            return tenantId + ":" + orderNo;
        }

        private static String txnKey(Long tenantId, String channelCode, String channelTransactionNo) {
            return tenantId + ":" + channelCode + ":" + channelTransactionNo;
        }
    }

    private static final class SequencePaymentNoGenerator implements com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator {

        private int sequence = 30000;

        @Override
        public String nextPaymentNo() {
            sequence++;
            return "PAY-" + sequence;
        }
    }

    private static final class StubOrderCommandFacade implements OrderCommandFacade {

        private int markPaidCount;
        private int markFailedCount;

        @Override
        public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount,
                             Instant paidTime) {
            markPaidCount++;
        }

        @Override
        public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                      Instant failedTime) {
            markFailedCount++;
        }

        @Override
        public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
            // not used by payment tests
        }
    }
}
