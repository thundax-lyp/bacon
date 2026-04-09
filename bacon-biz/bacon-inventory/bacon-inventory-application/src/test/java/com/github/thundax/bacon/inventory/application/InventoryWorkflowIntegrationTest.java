package com.github.thundax.bacon.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
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
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
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
                repository, repository, operationLogService, new SequenceInventoryReservationNoGenerator());

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<InventoryReservationResultDTO> first = CompletableFuture.supplyAsync(
                    () -> {
                        await(start);
                        return service.reserveStock(
                                TenantId.of(1001L),
                                OrderNo.of("ORDER-C1"),
                                List.of(new InventoryReservationItemDTO(101L, 40)));
                    },
                    pool);
            CompletableFuture<InventoryReservationResultDTO> second = CompletableFuture.supplyAsync(
                    () -> {
                        await(start);
                        return service.reserveStock(
                                TenantId.of(1001L),
                                OrderNo.of("ORDER-C2"),
                                List.of(new InventoryReservationItemDTO(101L, 40)));
                    },
                    pool);

            start.countDown();

            InventoryReservationResultDTO firstResult = first.get(5, TimeUnit.SECONDS);
            InventoryReservationResultDTO secondResult = second.get(5, TimeUnit.SECONDS);

            assertEquals(InventoryReservationStatus.RESERVED.value(), firstResult.getReservationStatus());
            assertEquals(InventoryReservationStatus.RESERVED.value(), secondResult.getReservationStatus());

            Inventory inventory =
                    repository.findInventory(TenantId.of(1001L), SkuId.of(101L)).orElseThrow();
            assertEquals(80, inventory.getReservedQuantity());
            assertEquals(20, inventory.getAvailableQuantity());
            assertTrue(inventory.getVersion() >= 2);

            assertNotNull(repository
                    .findReservation(TenantId.of(1001L), OrderNo.of("ORDER-C1"))
                    .orElse(null));
            assertNotNull(repository
                    .findReservation(TenantId.of(1001L), OrderNo.of("ORDER-C2"))
                    .orElse(null));
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
        repository.saveAuditOutbox(new InventoryAuditOutbox(
                null,
                null,
                TenantId.of(1001L),
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
                now));

        retryService.retryAuditOutbox();

        assertEquals(1, repository.deadLetterCount());
        assertTrue(repository
                .findRetryableAuditOutbox(Instant.now().plusSeconds(3600), 10)
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
        repository.saveAuditOutbox(new InventoryAuditOutbox(
                null,
                null,
                TenantId.of(1001L),
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
                now));

        retryService.retryAuditOutbox();

        assertTrue(repository
                .findRetryableAuditOutbox(Instant.now().plusSeconds(3600), 10)
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
                            TenantId.of(1001L),
                            SkuId.of(101L),
                            WarehouseCode.of("DEFAULT"),
                            100,
                            0,
                            100,
                            InventoryStatus.ENABLED,
                            0L,
                            Instant.parse("2026-03-26T09:59:00Z")));
        }

        @Override
        public Optional<Inventory> findInventory(TenantId tenantId, SkuId skuId) {
            return Optional.ofNullable(inventories.get(
                            key(tenantId == null ? null : tenantId.value(), skuId == null ? null : skuId.value())))
                    .map(this::copy);
        }

        @Override
        public List<Inventory> findInventories(TenantId tenantId) {
            return inventories.values().stream()
                    .filter(item -> java.util.Objects.equals(item.getTenantId(), tenantId))
                    .map(this::copy)
                    .toList();
        }

        @Override
        public List<Inventory> findInventories(TenantId tenantId, Set<SkuId> skuIds) {
            return skuIds.stream()
                    .map(skuId -> inventories.get(
                            key(tenantId == null ? null : tenantId.value(), skuId == null ? null : skuId.value())))
                    .filter(java.util.Objects::nonNull)
                    .map(this::copy)
                    .toList();
        }

        @Override
        public List<Inventory> pageInventories(
                TenantId tenantId, SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
            return findInventories(tenantId).stream()
                    .filter(item -> skuId == null || java.util.Objects.equals(item.getSkuId(), skuId))
                    .filter(item -> status == null || status.equals(item.getStatus()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long countInventories(TenantId tenantId, SkuId skuId, InventoryStatus status) {
            return pageInventories(tenantId, skuId, status, 1, Integer.MAX_VALUE)
                    .size();
        }

        @Override
        public synchronized Inventory saveInventory(Inventory inventory) {
            Inventory current = inventories.get(key(
                    inventory.getTenantId().value(),
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
            persisted.markPersisted(current.getVersion() + 1);
            inventories.put(
                    key(
                            persisted.getTenantId().value(),
                            persisted.getSkuId() == null
                                    ? null
                                    : persisted.getSkuId().value()),
                    persisted);
            return copy(persisted);
        }

        @Override
        public InventoryReservation saveReservation(InventoryReservation reservation) {
            String key = reservationKey(
                    reservation.getTenantId() == null
                            ? null
                            : reservation.getTenantId().value(),
                    reservation.getOrderNoValue());
            InventoryReservation existing = reservations.get(key);
            if (existing != null && !existing.getReservationNo().equals(reservation.getReservationNo())) {
                throw new DuplicateKeyException("duplicate orderNo");
            }
            reservations.put(key, reservation);
            return reservation;
        }

        @Override
        public Optional<InventoryReservation> findReservation(TenantId tenantId, OrderNo orderNo) {
            return Optional.ofNullable(reservations.get(reservationKey(
                    tenantId == null ? null : tenantId.value(), orderNo == null ? null : orderNo.value())));
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
            ledgers.computeIfAbsent(
                            reservationKey(
                                    ledger.getTenantId() == null
                                            ? null
                                            : ledger.getTenantId().value(),
                                    ledger.getOrderNoValue()),
                            ignored -> new ArrayList<>())
                    .add(ledger);
        }

        @Override
        public List<InventoryLedger> findLedgers(TenantId tenantId, OrderNo orderNo) {
            return List.copyOf(ledgers.getOrDefault(
                    reservationKey(
                            tenantId == null ? null : tenantId.value(), orderNo == null ? null : orderNo.value()),
                    List.of()));
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
            if (failAuditPersist) {
                throw new RuntimeException("force-fail-audit");
            }
            auditLogs
                    .computeIfAbsent(
                            reservationKey(
                                    auditLog.getTenantId() == null
                                            ? null
                                            : auditLog.getTenantId().value(),
                                    auditLog.getOrderNoValue()),
                            ignored -> new ArrayList<>())
                    .add(auditLog);
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(TenantId tenantId, OrderNo orderNo) {
            return List.copyOf(auditLogs.getOrDefault(
                    reservationKey(
                            tenantId == null ? null : tenantId.value(), orderNo == null ? null : orderNo.value()),
                    List.of()));
        }

        @Override
        public void saveAuditOutbox(InventoryAuditOutbox outbox) {
            if (outbox.getId() == null) {
                outbox.setId(OutboxId.of(outboxIdGenerator.incrementAndGet()));
            }
            if (outbox.getEventCode() == null) {
                outbox.setEventCode(generateEventCode());
            }
            outboxMap.put(outbox.getId(), outbox);
        }

        @Override
        public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
            return outboxMap.values().stream()
                    .filter(item -> InventoryAuditOutboxStatus.NEW.equals(item.getStatus())
                            || InventoryAuditOutboxStatus.RETRYING.equals(item.getStatus()))
                    .filter(item -> item.getNextRetryAt() == null
                            || !item.getNextRetryAt().isAfter(now))
                    .sorted(java.util.Comparator.comparing(InventoryAuditOutbox::getFailedAt)
                            .thenComparing(InventoryAuditOutbox::getIdValue))
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<InventoryAuditOutbox> claimRetryableAuditOutbox(
                Instant now, int limit, String processingOwner, Instant leaseUntil) {
            List<InventoryAuditOutbox> claimed = new ArrayList<>();
            for (InventoryAuditOutbox item : findRetryableAuditOutbox(now, Math.max(limit * 3, limit))) {
                if (claimed.size() >= limit) {
                    break;
                }
                if (!tryClaim(item.getId(), now, processingOwner, leaseUntil)) {
                    continue;
                }
                InventoryAuditOutbox current = outboxMap.get(item.getId());
                if (current != null) {
                    claimed.add(current);
                }
            }
            return List.copyOf(claimed);
        }

        @Override
        public int releaseExpiredAuditOutboxLease(Instant now) {
            int released = 0;
            for (InventoryAuditOutbox item : outboxMap.values()) {
                if (!InventoryAuditOutboxStatus.PROCESSING.equals(item.getStatus())) {
                    continue;
                }
                if (item.getLeaseUntil() == null || item.getLeaseUntil().isAfter(now)) {
                    continue;
                }
                item.setStatus(InventoryAuditOutboxStatus.RETRYING);
                item.setProcessingOwner(null);
                item.setLeaseUntil(null);
                item.setClaimedAt(null);
                item.setUpdatedAt(now);
                released++;
            }
            return released;
        }

        @Override
        public void updateAuditOutboxForRetry(
                OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox != null) {
                outbox.setStatus(InventoryAuditOutboxStatus.RETRYING);
                outbox.setRetryCount(retryCount);
                outbox.setNextRetryAt(nextRetryAt);
                outbox.setErrorMessage(errorMessage);
                outbox.setUpdatedAt(updatedAt);
            }
        }

        @Override
        public boolean updateAuditOutboxForRetryClaimed(
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
            outbox.setStatus(InventoryAuditOutboxStatus.RETRYING);
            outbox.setRetryCount(retryCount);
            outbox.setNextRetryAt(nextRetryAt);
            outbox.setErrorMessage(errorMessage);
            outbox.setProcessingOwner(null);
            outbox.setLeaseUntil(null);
            outbox.setClaimedAt(null);
            outbox.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public void markAuditOutboxDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox != null) {
                outbox.setStatus(InventoryAuditOutboxStatus.DEAD);
                outbox.setRetryCount(retryCount);
                outbox.setDeadReason(deadReason);
                outbox.setUpdatedAt(updatedAt);
            }
        }

        @Override
        public boolean markAuditOutboxDeadClaimed(
                OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox == null
                    || !InventoryAuditOutboxStatus.PROCESSING.equals(outbox.getStatus())
                    || !processingOwner.equals(outbox.getProcessingOwner())) {
                return false;
            }
            outbox.setStatus(InventoryAuditOutboxStatus.DEAD);
            outbox.setRetryCount(retryCount);
            outbox.setDeadReason(deadReason);
            outbox.setProcessingOwner(null);
            outbox.setLeaseUntil(null);
            outbox.setClaimedAt(null);
            outbox.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public void deleteAuditOutbox(OutboxId outboxId) {
            outboxMap.remove(outboxId);
        }

        @Override
        public boolean deleteAuditOutboxClaimed(OutboxId outboxId, String processingOwner) {
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
        public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
            deadLetterMap.put(deadLetter.getOutboxId(), deadLetter);
        }

        private int deadLetterCount() {
            return deadLetterMap.size();
        }

        private Inventory copy(Inventory source) {
            return Inventory.reconstruct(
                    source.getId(),
                    source.getTenantId(),
                    source.getSkuId(),
                    source.getWarehouseCode(),
                    source.getOnHandQuantity(),
                    source.getReservedQuantity(),
                    source.getAvailableQuantity(),
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
            outbox.setStatus(InventoryAuditOutboxStatus.PROCESSING);
            outbox.setProcessingOwner(processingOwner);
            outbox.setLeaseUntil(leaseUntil);
            outbox.setClaimedAt(now);
            outbox.setUpdatedAt(now);
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
