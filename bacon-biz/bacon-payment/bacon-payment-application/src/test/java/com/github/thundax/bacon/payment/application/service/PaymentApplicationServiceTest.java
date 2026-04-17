package com.github.thundax.bacon.payment.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.payment.application.command.PaymentCloseResult;
import com.github.thundax.bacon.payment.application.command.PaymentCreateResult;
import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.audit.PaymentOperationLogSupport;
import com.github.thundax.bacon.payment.application.command.PaymentCallbackApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PaymentApplicationServiceTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    private PaymentOperationLogSupport paymentOperationLogSupport(TestPaymentRepository repository) {
        IdGenerator idGenerator = bizTag -> 1L;
        return new PaymentOperationLogSupport(repository, idGenerator);
    }

    private IdGenerator idGenerator() {
        return bizTag -> 1L;
    }

    @Test
    void createPaymentShouldGeneratePaymentNoInsideModule() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        TestPaymentRepository repository = new TestPaymentRepository();
        PaymentCreateApplicationService service = new PaymentCreateApplicationService(
                repository, paymentOperationLogSupport(repository), () -> "PAY-20001", idGenerator());

        PaymentCreateResult result = service.createPayment(
                "ORD-10001",
                2001L,
                BigDecimal.TEN,
                "MOCK",
                "test",
                Instant.now().plusSeconds(1800));

        assertEquals("PAY-20001", result.getPaymentNo());
        assertEquals("ORD-10001", result.getOrderNo());
        assertEquals(PaymentStatus.PAYING.value(), result.getPaymentStatus());
        assertEquals(1, repository.findAuditLogsByPaymentNo("PAY-20001").size());
    }

    @Test
    void createPaymentShouldBeIdempotentByOrderNo() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        TestPaymentRepository repository = new TestPaymentRepository();
        PaymentCreateApplicationService service = new PaymentCreateApplicationService(
                repository, paymentOperationLogSupport(repository), new SequencePaymentNoGenerator(), idGenerator());

        PaymentCreateResult first = service.createPayment(
                "ORD-10002",
                2001L,
                BigDecimal.ONE,
                "MOCK",
                "test",
                Instant.now().plusSeconds(1800));
        PaymentCreateResult second = service.createPayment(
                "ORD-10002",
                2001L,
                BigDecimal.ONE,
                "MOCK",
                "test",
                Instant.now().plusSeconds(1800));

        assertEquals(first.getPaymentNo(), second.getPaymentNo());
        assertEquals(
                1, repository.findAuditLogsByPaymentNo(first.getPaymentNo()).size());
    }

    @Test
    void callbackAndCloseShouldRespectStateRules() {
        BaconContextHolder.set(new BaconContext(1001L, 2003L));
        TestPaymentRepository repository = new TestPaymentRepository();
        StubOrderCommandFacade orderCommandFacade = new StubOrderCommandFacade();
        PaymentCreateApplicationService createService = new PaymentCreateApplicationService(
                repository, paymentOperationLogSupport(repository), () -> "PAY-20003", idGenerator());
        PaymentCallbackApplicationService callbackService = new PaymentCallbackApplicationService(
                repository, repository, paymentOperationLogSupport(repository), orderCommandFacade, idGenerator());
        PaymentQueryApplicationService queryService = new PaymentQueryApplicationService(repository, repository);
        PaymentCloseApplicationService closeService =
                new PaymentCloseApplicationService(repository, paymentOperationLogSupport(repository));

        PaymentCreateResult created = createService.createPayment(
                "ORD-10003",
                2003L,
                new BigDecimal("18.80"),
                "MOCK",
                "callback",
                Instant.now().plusSeconds(1800));
        callbackService.callbackPaid(
                "MOCK", created.getPaymentNo(), "TXN-1", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}");
        callbackService.callbackFailed(
                "MOCK", created.getPaymentNo(), "FAILED", "{\"tradeStatus\":\"FAILED\"}", "CHANNEL_FAIL");
        PaymentDetailDTO detail = queryService.getByPaymentNo(created.getPaymentNo());

        assertEquals(PaymentStatus.PAID.value(), detail.getPaymentStatus());
        assertEquals("TXN-1", detail.getChannelTransactionNo());
        assertEquals("SUCCESS", detail.getChannelStatus());
        assertEquals(1, orderCommandFacade.markPaidCount);
        assertEquals(0, orderCommandFacade.markFailedCount);

        assertEquals(
                "FAILED",
                closeService
                        .closePayment(created.getPaymentNo(), "USER_CANCELLED")
                        .getCloseResult());
    }

    @Test
    void duplicateOrIgnoredCallbacksShouldStillWriteAuditLog() {
        BaconContextHolder.set(new BaconContext(1001L, 2007L));
        TestPaymentRepository repository = new TestPaymentRepository();
        StubOrderCommandFacade orderCommandFacade = new StubOrderCommandFacade();
        PaymentCreateApplicationService createService = new PaymentCreateApplicationService(
                repository, paymentOperationLogSupport(repository), () -> "PAY-20007", idGenerator());
        PaymentCallbackApplicationService callbackService = new PaymentCallbackApplicationService(
                repository, repository, paymentOperationLogSupport(repository), orderCommandFacade, idGenerator());

        PaymentCreateResult created = createService.createPayment(
                "ORD-10007",
                2007L,
                new BigDecimal("20.00"),
                "MOCK",
                "duplicate-callback",
                Instant.now().plusSeconds(1800));
        callbackService.callbackPaid(
                "MOCK", created.getPaymentNo(), "TXN-20007", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}");
        callbackService.callbackPaid(
                "MOCK", created.getPaymentNo(), "TXN-20007", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}");
        callbackService.callbackFailed(
                "MOCK", created.getPaymentNo(), "FAILED", "{\"tradeStatus\":\"FAILED\"}", "CHANNEL_FAIL");

        List<PaymentAuditLog> auditLogs = repository.findAuditLogsByPaymentNo(created.getPaymentNo());

        assertEquals(4, auditLogs.size());
        assertEquals(PaymentAuditActionType.CREATE, auditLogs.get(0).getActionType());
        assertEquals(PaymentAuditActionType.CALLBACK_PAID, auditLogs.get(1).getActionType());
        assertEquals(PaymentAuditActionType.CALLBACK_PAID, auditLogs.get(2).getActionType());
        assertEquals(PaymentStatus.PAID, auditLogs.get(2).getBeforeStatus());
        assertEquals(PaymentStatus.PAID, auditLogs.get(2).getAfterStatus());
        assertEquals(PaymentAuditActionType.CALLBACK_FAILED, auditLogs.get(3).getActionType());
        assertEquals(PaymentStatus.PAID, auditLogs.get(3).getBeforeStatus());
        assertEquals(PaymentStatus.PAID, auditLogs.get(3).getAfterStatus());
        assertEquals(1, orderCommandFacade.markPaidCount);
        assertEquals(0, orderCommandFacade.markFailedCount);
    }

    @Test
    void closePaymentShouldBeIdempotentForClosedPayment() {
        BaconContextHolder.set(new BaconContext(1001L, 2004L));
        TestPaymentRepository repository = new TestPaymentRepository();
        PaymentCreateApplicationService createService = new PaymentCreateApplicationService(
                repository, paymentOperationLogSupport(repository), () -> "PAY-20004", idGenerator());
        PaymentCloseApplicationService closeService =
                new PaymentCloseApplicationService(repository, paymentOperationLogSupport(repository));

        PaymentCreateResult created = createService.createPayment(
                "ORD-10004",
                2004L,
                new BigDecimal("28.00"),
                "MOCK",
                "close",
                Instant.now().plusSeconds(1800));
        PaymentCloseResult first = closeService.closePayment(created.getPaymentNo(), "SYSTEM_CANCELLED");
        PaymentCloseResult second = closeService.closePayment(created.getPaymentNo(), "SYSTEM_CANCELLED");

        assertEquals("SUCCESS", first.getCloseResult());
        assertEquals("SUCCESS", second.getCloseResult());
        assertEquals(PaymentStatus.CLOSED.value(), second.getPaymentStatus());
        assertEquals(
                2, repository.findAuditLogsByPaymentNo(created.getPaymentNo()).size());
    }

    @Test
    void createPaymentShouldNotRollbackWhenAuditWriteFails() {
        BaconContextHolder.set(new BaconContext(1001L, 2005L));
        TestPaymentRepository repository = new TestPaymentRepository();
        repository.failAuditSave = true;
        PaymentCreateApplicationService service = new PaymentCreateApplicationService(
                repository, paymentOperationLogSupport(repository), () -> "PAY-20005", idGenerator());

        PaymentCreateResult result = assertDoesNotThrow(() -> service.createPayment(
                "ORD-10005",
                2005L,
                BigDecimal.ONE,
                "MOCK",
                "audit-fail",
                Instant.now().plusSeconds(1800)));

        assertEquals("PAY-20005", result.getPaymentNo());
        assertEquals(
                PaymentStatus.PAYING,
                repository.findOrderByPaymentNo("PAY-20005").orElseThrow().getPaymentStatus());
        assertEquals(0, repository.findAuditLogsByPaymentNo("PAY-20005").size());
    }

    @Test
    void callbackPaidShouldNotRollbackWhenAuditWriteFails() {
        BaconContextHolder.set(new BaconContext(1001L, 2006L));
        TestPaymentRepository repository = new TestPaymentRepository();
        StubOrderCommandFacade orderCommandFacade = new StubOrderCommandFacade();
        PaymentCreateApplicationService createService = new PaymentCreateApplicationService(
                repository, paymentOperationLogSupport(repository), () -> "PAY-20006", idGenerator());
        createService.createPayment(
                "ORD-10006",
                2006L,
                new BigDecimal("18.00"),
                "MOCK",
                "audit-fail-callback",
                Instant.now().plusSeconds(1800));
        repository.failAuditSave = true;
        PaymentCallbackApplicationService callbackService = new PaymentCallbackApplicationService(
                repository, repository, paymentOperationLogSupport(repository), orderCommandFacade, idGenerator());

        assertDoesNotThrow(() -> callbackService.callbackPaid(
                "MOCK", "PAY-20006", "TXN-20006", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}"));

        PaymentOrder paymentOrder = repository.findOrderByPaymentNo("PAY-20006").orElseThrow();
        assertEquals(PaymentStatus.PAID, paymentOrder.getPaymentStatus());
        assertEquals(1, orderCommandFacade.markPaidCount);
    }

    private static final class TestPaymentRepository
            implements PaymentOrderRepository, PaymentCallbackRecordRepository, PaymentAuditLogRepository {

        private final ConcurrentMap<String, PaymentOrder> paymentsByPaymentNo = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, PaymentOrder> paymentsByOrderNo = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, List<PaymentCallbackRecord>> callbacksByPaymentNo =
                new ConcurrentHashMap<>();
        private final ConcurrentMap<String, PaymentCallbackRecord> callbacksByTxn = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, List<PaymentAuditLog>> auditLogsByPaymentNo = new ConcurrentHashMap<>();
        private boolean failAuditSave;

        @Override
        public PaymentOrder save(PaymentOrder paymentOrder) {
            paymentsByPaymentNo.put(
                    paymentKey(currentTenantId(), paymentOrder.getPaymentNo().value()), paymentOrder);
            paymentsByOrderNo.put(
                    orderKey(currentTenantId(), paymentOrder.getOrderNo().value()), paymentOrder);
            return paymentOrder;
        }

        @Override
        public Optional<PaymentOrder> findOrderByPaymentNo(String paymentNo) {
            return Optional.ofNullable(paymentsByPaymentNo.get(paymentKey(currentTenantId(), paymentNo)));
        }

        @Override
        public Optional<PaymentOrder> findOrderByOrderNo(String orderNo) {
            return Optional.ofNullable(paymentsByOrderNo.get(orderKey(currentTenantId(), orderNo)));
        }

        @Override
        public PaymentCallbackRecord save(PaymentCallbackRecord callbackRecord) {
            callbacksByPaymentNo
                    .computeIfAbsent(
                            paymentKey(
                                    currentTenantId(),
                                    callbackRecord.getPaymentNo().value()),
                            ignored -> new ArrayList<>())
                    .add(callbackRecord);
            if (callbackRecord.getChannelTransactionNo() != null) {
                callbacksByTxn.put(
                        txnKey(
                                currentTenantId(),
                                callbackRecord.getChannelCode().value(),
                                callbackRecord.getChannelTransactionNo()),
                        callbackRecord);
            }
            return callbackRecord;
        }

        @Override
        public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(String paymentNo) {
            return findCallbacksByPaymentNo(paymentNo).stream()
                    .max(Comparator.comparing(PaymentCallbackRecord::getReceivedAt)
                            .thenComparing(PaymentCallbackRecord::getId));
        }

        @Override
        public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(
                String channelCode, String channelTransactionNo) {
            return Optional.ofNullable(
                    callbacksByTxn.get(txnKey(currentTenantId(), channelCode, channelTransactionNo)));
        }

        @Override
        public List<PaymentCallbackRecord> findCallbacksByPaymentNo(String paymentNo) {
            return List.copyOf(callbacksByPaymentNo.getOrDefault(paymentKey(currentTenantId(), paymentNo), List.of()));
        }

        @Override
        public void save(PaymentAuditLog auditLog) {
            if (failAuditSave) {
                throw new IllegalStateException("audit unavailable");
            }
            auditLogsByPaymentNo
                    .computeIfAbsent(
                            paymentKey(
                                    currentTenantId(), auditLog.getPaymentNo().value()),
                            ignored -> new ArrayList<>())
                    .add(auditLog);
        }

        @Override
        public List<PaymentAuditLog> findAuditLogsByPaymentNo(String paymentNo) {
            return List.copyOf(auditLogsByPaymentNo.getOrDefault(paymentKey(currentTenantId(), paymentNo), List.of()));
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

        private static Long currentTenantId() {
            return BaconContextHolder.requireTenantId();
        }
    }

    private static final class SequencePaymentNoGenerator
            implements com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator {

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
        public void markPaid(OrderMarkPaidFacadeRequest request) {
            markPaidCount++;
        }

        @Override
        public void markPaymentFailed(OrderMarkPaymentFailedFacadeRequest request) {
            markFailedCount++;
        }

        @Override
        public void closeExpiredOrder(OrderCloseExpiredFacadeRequest request) {
            // not used by payment tests
        }
    }
}
