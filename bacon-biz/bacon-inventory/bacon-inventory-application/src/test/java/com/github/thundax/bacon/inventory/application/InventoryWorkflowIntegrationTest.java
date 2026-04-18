package com.github.thundax.bacon.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.AsyncTaskWrapper;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditOutboxRetrier;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.command.InventoryReservationApplicationService;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

class InventoryWorkflowIntegrationTest {

    private static final IdGenerator ID_GENERATOR = bizTag -> 1L;

    @Test
    void shouldHandleConcurrentReserveWithOptimisticRetry() throws Exception {
        OptimisticInventoryRepository repository = new OptimisticInventoryRepository(false);
        InventoryOperationLogSupport operationLogService =
                new InventoryOperationLogSupport(repository, repository, ID_GENERATOR);
        InventoryReservationApplicationService service = new InventoryReservationApplicationService(
                repository,
                repository,
                operationLogService,
                new SequenceInventoryReservationNoGenerator(),
                ID_GENERATOR);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Supplier<InventoryReservationResult> firstTask = BaconContextHolder.callWithTenantId(
                    1001L,
                    () -> AsyncTaskWrapper.wrap((Supplier<InventoryReservationResult>) () -> {
                        await(start);
                        return service.reserveStock(
                                OrderNo.of("ORDER-C1"), List.of(new InventoryReservationItemDTO(101L, 40)));
                    }));
            CompletableFuture<InventoryReservationResult> first = CompletableFuture.supplyAsync(firstTask, pool);
            Supplier<InventoryReservationResult> secondTask = BaconContextHolder.callWithTenantId(
                    1001L,
                    () -> AsyncTaskWrapper.wrap((Supplier<InventoryReservationResult>) () -> {
                        await(start);
                        return service.reserveStock(
                                OrderNo.of("ORDER-C2"), List.of(new InventoryReservationItemDTO(101L, 40)));
                    }));
            CompletableFuture<InventoryReservationResult> second = CompletableFuture.supplyAsync(secondTask, pool);

            start.countDown();

            InventoryReservationResult firstResult = first.get(5, TimeUnit.SECONDS);
            InventoryReservationResult secondResult = second.get(5, TimeUnit.SECONDS);

            assertEquals(InventoryReservationStatus.RESERVED.value(), firstResult.getReservationStatus());
            assertEquals(InventoryReservationStatus.RESERVED.value(), secondResult.getReservationStatus());

            Inventory inventory = repository.findBySkuId(SkuId.of(101L)).orElseThrow();
            assertEquals(80, inventory.getReservedQuantity().value());
            assertEquals(20, inventory.availableQuantity().value());
            assertTrue(inventory.getVersion().value() >= 2);

            BaconContextHolder.runWithTenantId(
                    1001L,
                    () -> assertNotNull(
                            repository.findByOrderNo(OrderNo.of("ORDER-C1")).orElse(null)));
            BaconContextHolder.runWithTenantId(
                    1001L,
                    () -> assertNotNull(
                            repository.findByOrderNo(OrderNo.of("ORDER-C2")).orElse(null)));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void shouldMoveOutboxToDeadLetterAfterRetryExhausted() {
        OptimisticInventoryRepository repository = new OptimisticInventoryRepository(true);
        InventoryAuditOutboxRetrier retryService =
                new InventoryAuditOutboxRetrier(repository, repository, repository, bizTag -> 2001L);
        ReflectionTestUtils.setField(retryService, "enabled", true);
        ReflectionTestUtils.setField(retryService, "batchSize", 10);
        ReflectionTestUtils.setField(retryService, "maxRetries", 1);
        ReflectionTestUtils.setField(retryService, "baseDelaySeconds", 1L);
        ReflectionTestUtils.setField(retryService, "maxDelaySeconds", 10L);

        Instant now = Instant.parse("2026-03-26T10:00:00Z");
        BaconContextHolder.runWithTenantId(
                1001L,
                () -> repository.insert(InventoryAuditOutbox.create(
                        OutboxId.of(1001L),
                        null,
                        OrderNo.of("ORDER-DEAD"),
                        ReservationNo.of("RSV-DEAD"),
                        InventoryAuditActionType.RESERVE,
                        InventoryAuditOperatorType.SYSTEM,
                        String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                        now,
                        "INIT",
                        InventoryAuditOutboxStatus.NEW,
                        1,
                        Instant.EPOCH,
                        null,
                        null,
                        null,
                        null,
                        now,
                        now)));

        retryService.retryAuditOutbox();

        assertEquals(1, repository.deadLetterCount());
        assertTrue(repository
                .findRetryable(Instant.now().plusSeconds(3600), 10)
                .isEmpty());
    }

    @Test
    void shouldDeleteOutboxAfterRetrySuccess() {
        OptimisticInventoryRepository repository = new OptimisticInventoryRepository(false);
        InventoryAuditOutboxRetrier retryService =
                new InventoryAuditOutboxRetrier(repository, repository, repository, bizTag -> 2001L);
        ReflectionTestUtils.setField(retryService, "enabled", true);
        ReflectionTestUtils.setField(retryService, "batchSize", 10);
        ReflectionTestUtils.setField(retryService, "maxRetries", 3);
        ReflectionTestUtils.setField(retryService, "baseDelaySeconds", 1L);
        ReflectionTestUtils.setField(retryService, "maxDelaySeconds", 10L);

        Instant now = Instant.parse("2026-03-26T10:00:00Z");
        BaconContextHolder.runWithTenantId(
                1001L,
                () -> repository.insert(InventoryAuditOutbox.create(
                        OutboxId.of(1002L),
                        null,
                        OrderNo.of("ORDER-OK"),
                        ReservationNo.of("RSV-OK"),
                        InventoryAuditActionType.RESERVE,
                        InventoryAuditOperatorType.SYSTEM,
                        String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                        now,
                        "INIT",
                        InventoryAuditOutboxStatus.NEW,
                        0,
                        Instant.EPOCH,
                        null,
                        null,
                        null,
                        null,
                        now,
                        now)));

        retryService.retryAuditOutbox();

        assertTrue(repository
                .findRetryable(Instant.now().plusSeconds(3600), 10)
                .isEmpty());
        assertEquals(0, repository.deadLetterCount());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    private static final class OptimisticInventoryRepository
            implements InventoryStockRepository,
                    InventoryReservationRepository,
                    InventoryAuditRecordRepository,
                    InventoryAuditOutboxRepository,
                    InventoryAuditDeadLetterRepository {

        private static final DateTimeFormatter EVENT_CODE_TIMESTAMP_FORMATTER =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
        private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();
        private final Map<OutboxId, InventoryAuditOutbox> outboxMap = new ConcurrentHashMap<>();
        private final Map<OutboxId, TenantId> outboxTenantMap = new ConcurrentHashMap<>();
        private final Map<OutboxId, InventoryAuditDeadLetter> deadLetterMap = new ConcurrentHashMap<>();
        private final AtomicLong outboxIdGenerator = new AtomicLong(1000L);
        private final AtomicLong outboxEventCodeGenerator = new AtomicLong(1000L);
        private final AtomicLong deadLetterIdGenerator = new AtomicLong(2000L);
        private final boolean failAuditPersist;

        private OptimisticInventoryRepository(boolean failAuditPersist) {
            this.failAuditPersist = failAuditPersist;
            inventories.put(
                    key(1001L, 101L),
                    Inventory.reconstruct(
                            InventoryId.of(1L),
                            SkuId.of(101L),
                            WarehouseCode.of("DEFAULT"),
                            new OnHandQuantity(100),
                            new ReservedQuantity(0),
                            InventoryStatus.ENABLED,
                            new Version(0L),
                            Instant.parse("2026-03-26T09:59:00Z")));
        }

        @Override
        public Optional<Inventory> findBySkuId(SkuId skuId) {
            return Optional.ofNullable(inventories.values().stream()
                            .filter(item -> java.util.Objects.equals(item.getSkuId(), skuId))
                            .findFirst()
                            .orElse(null))
                    .map(this::copy);
        }

        @Override
        public List<Inventory> list() {
            return inventories.values().stream().map(this::copy).toList();
        }

        @Override
        public List<Inventory> listBySkuIds(Set<SkuId> skuIds) {
            return skuIds.stream()
                    .map(this::findBySkuId)
                    .flatMap(Optional::stream)
                    .toList();
        }

        @Override
        public List<Inventory> page(SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
            return list().stream()
                    .filter(item -> skuId == null || java.util.Objects.equals(item.getSkuId(), skuId))
                    .filter(item -> status == null || status.equals(item.getStatus()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long count(SkuId skuId, InventoryStatus status) {
            return page(skuId, status, 1, Integer.MAX_VALUE).size();
        }

        @Override
        public synchronized Inventory insert(Inventory inventory) {
            Inventory persisted = copy(inventory);
            persisted.markPersisted(new Version(0L));
            inventories.put(
                    key(
                            1001L,
                            persisted.getSkuId() == null
                                    ? null
                                    : persisted.getSkuId().value()),
                    persisted);
            return copy(persisted);
        }

        @Override
        public synchronized Inventory update(Inventory inventory) {
            Inventory current = inventories.get(key(
                    1001L,
                    inventory.getSkuId() == null ? null : inventory.getSkuId().value()));
            if (current == null) {
                throw new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(
                                inventory.getSkuId() == null
                                        ? null
                                        : inventory.getSkuId().value()));
            }
            if (!current.getVersion().equals(inventory.getVersion())) {
                throw new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED,
                        String.valueOf(
                                inventory.getSkuId() == null
                                        ? null
                                        : inventory.getSkuId().value()));
            }
            Inventory persisted = copy(inventory);
            persisted.markPersisted(current.getVersion().next());
            inventories.put(
                    key(
                            1001L,
                            persisted.getSkuId() == null
                                    ? null
                                    : persisted.getSkuId().value()),
                    persisted);
            return copy(persisted);
        }

        @Override
        public InventoryReservation insert(InventoryReservation reservation) {
            String key = reservationKey(
                    BaconContextHolder.currentTenantId(),
                    reservation.getOrderNo() == null
                            ? null
                            : reservation.getOrderNo().value());
            InventoryReservation existing = reservations.get(key);
            if (existing != null && !existing.getReservationNo().equals(reservation.getReservationNo())) {
                throw new DuplicateKeyException("duplicate orderNo");
            }
            reservations.put(key, reservation);
            return reservation;
        }

        @Override
        public InventoryReservation update(InventoryReservation reservation) {
            String key = reservationKey(
                    BaconContextHolder.currentTenantId(),
                    reservation.getOrderNo() == null
                            ? null
                            : reservation.getOrderNo().value());
            InventoryReservation existing = reservations.get(key);
            if (existing != null && !existing.getReservationNo().equals(reservation.getReservationNo())) {
                throw new DuplicateKeyException("duplicate orderNo");
            }
            reservations.put(key, reservation);
            return reservation;
        }

        @Override
        public Optional<InventoryReservation> findByOrderNo(OrderNo orderNo) {
            return Optional.ofNullable(reservations.get(
                    reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value())));
        }

        @Override
        public void insertLedger(InventoryLedger ledger) {
            ledgers.computeIfAbsent(
                            reservationKey(
                                    BaconContextHolder.currentTenantId(),
                                    ledger.getOrderNo() == null
                                            ? null
                                            : ledger.getOrderNo().value()),
                            ignored -> new ArrayList<>())
                    .add(ledger);
        }

        @Override
        public List<InventoryLedger> listLedgers(OrderNo orderNo) {
            return List.copyOf(ledgers.getOrDefault(
                    reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value()),
                    List.of()));
        }

        @Override
        public void insertLog(InventoryAuditLog auditLog) {
            if (failAuditPersist) {
                throw new RuntimeException("force-fail-audit");
            }
            auditLogs
                    .computeIfAbsent(
                            reservationKey(
                                    BaconContextHolder.currentTenantId(),
                                    auditLog.getOrderNo() == null
                                            ? null
                                            : auditLog.getOrderNo().value()),
                            ignored -> new ArrayList<>())
                    .add(auditLog);
        }

        @Override
        public List<InventoryAuditLog> listLogs(OrderNo orderNo) {
            return List.copyOf(auditLogs.getOrDefault(
                    reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value()),
                    List.of()));
        }

        @Override
        public void insert(InventoryAuditOutbox outbox) {
            if (outbox.getId() == null) {
                throw new IllegalArgumentException("outbox.id must not be null");
            }
            if (outbox.getEventCode() == null) {
                outbox.assignEventCode(generateEventCode());
            }
            outboxMap.put(outbox.getId(), outbox);
            Long tenantId = BaconContextHolder.currentTenantId();
            if (tenantId != null) {
                outboxTenantMap.put(outbox.getId(), TenantId.of(tenantId));
            }
        }

        @Override
        public List<InventoryAuditOutbox> findRetryable(Instant now, int limit) {
            return outboxMap.values().stream()
                    .filter(item -> InventoryAuditOutboxStatus.NEW.equals(item.getStatus())
                            || InventoryAuditOutboxStatus.RETRYING.equals(item.getStatus()))
                    .filter(item -> item.getNextRetryAt() == null
                            || !item.getNextRetryAt().isAfter(now))
                    .sorted(java.util.Comparator.comparing(InventoryAuditOutbox::getFailedAt)
                            .thenComparing(item ->
                                    item.getId() == null ? null : item.getId().value()))
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<TenantScopedAuditOutbox> claimRetryable(
                Instant now, int limit, String processingOwner, Instant leaseUntil) {
            List<TenantScopedAuditOutbox> claimed = new ArrayList<>();
            for (InventoryAuditOutbox item : findRetryable(now, Math.max(limit * 3, limit))) {
                if (claimed.size() >= limit) {
                    break;
                }
                if (!tryClaim(item.getId(), now, processingOwner, leaseUntil)) {
                    continue;
                }
                InventoryAuditOutbox current = outboxMap.get(item.getId());
                if (current != null) {
                    claimed.add(new TenantScopedAuditOutbox(outboxTenantMap.get(current.getId()), current));
                }
            }
            return List.copyOf(claimed);
        }

        @Override
        public int releaseExpiredLease(Instant now) {
            int released = 0;
            for (InventoryAuditOutbox item : outboxMap.values()) {
                if (!InventoryAuditOutboxStatus.PROCESSING.equals(item.getStatus())) {
                    continue;
                }
                if (item.getLeaseUntil() == null || item.getLeaseUntil().isAfter(now)) {
                    continue;
                }
                item.releaseLeaseToRetrying(now);
                released++;
            }
            return released;
        }

        @Override
        public void updateForRetry(
                OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox != null) {
                outbox.markRetrying(retryCount, nextRetryAt, errorMessage, updatedAt);
            }
        }

        @Override
        public boolean updateForRetryClaimed(
                OutboxId outboxId,
                String processingOwner,
                int retryCount,
                Instant nextRetryAt,
                String errorMessage,
                Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox == null
                    || !InventoryAuditOutboxStatus.PROCESSING.equals(outbox.getStatus())
                    || !processingOwner.equals(outbox.getProcessingOwner())) {
                return false;
            }
            outbox.markRetryingClaimed(retryCount, nextRetryAt, errorMessage, updatedAt);
            return true;
        }

        @Override
        public void markDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox != null) {
                outbox.markDead(retryCount, deadReason, updatedAt);
            }
        }

        @Override
        public boolean markDeadClaimed(
                OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox == null
                    || !InventoryAuditOutboxStatus.PROCESSING.equals(outbox.getStatus())
                    || !processingOwner.equals(outbox.getProcessingOwner())) {
                return false;
            }
            outbox.markDeadClaimed(retryCount, deadReason, updatedAt);
            return true;
        }

        @Override
        public void delete(OutboxId outboxId) {
            outboxMap.remove(outboxId);
        }

        @Override
        public boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox == null
                    || !InventoryAuditOutboxStatus.PROCESSING.equals(outbox.getStatus())
                    || !processingOwner.equals(outbox.getProcessingOwner())) {
                return false;
            }
            outboxMap.remove(outboxId);
            return true;
        }

        @Override
        public void insert(InventoryAuditDeadLetter deadLetter) {
            deadLetterMap.put(deadLetter.getOutboxId(), deadLetter);
        }

        private int deadLetterCount() {
            return deadLetterMap.size();
        }

        private Inventory copy(Inventory source) {
            return Inventory.reconstruct(
                    source.getId(),
                    source.getSkuId(),
                    source.getWarehouseCode(),
                    source.getOnHandQuantity(),
                    source.getReservedQuantity(),
                    source.getStatus(),
                    source.getVersion(),
                    source.getUpdatedAt());
        }

        private static String key(Long tenantId, Long skuId) {
            return tenantId + ":" + skuId;
        }

        private static String reservationKey(Long tenantId, String orderNo) {
            return tenantId + ":" + orderNo;
        }

        private boolean tryClaim(OutboxId outboxId, Instant now, String processingOwner, Instant leaseUntil) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox == null) {
                return false;
            }
            if (!InventoryAuditOutboxStatus.NEW.equals(outbox.getStatus())
                    && !InventoryAuditOutboxStatus.RETRYING.equals(outbox.getStatus())) {
                return false;
            }
            if (outbox.getNextRetryAt() != null && outbox.getNextRetryAt().isAfter(now)) {
                return false;
            }
            outbox.claim(processingOwner, leaseUntil, now);
            return true;
        }

        private EventCode generateEventCode() {
            long id = outboxEventCodeGenerator.incrementAndGet();
            String timestamp = LocalDateTime.now().format(EVENT_CODE_TIMESTAMP_FORMATTER);
            String suffix = String.format("%06d", Math.floorMod(id, 1_000_000L));
            return EventCode.of("EVT" + timestamp + "-" + suffix);
        }
    }

    private static final class SequenceInventoryReservationNoGenerator implements InventoryReservationNoGenerator {

        private final AtomicLong value = new AtomicLong(5000L);

        @Override
        public String nextReservationNo() {
            return "RSV-" + value.incrementAndGet();
        }
    }
}
