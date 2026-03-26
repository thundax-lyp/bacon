package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import java.time.Instant;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryWorkflowIntegrationTest {

    @Test
    void shouldHandleConcurrentReserveWithOptimisticRetry() throws Exception {
        OptimisticInventoryRepository repository = new OptimisticInventoryRepository(false);
        InventoryOperationLogService operationLogService = new InventoryOperationLogService(repository);
        InventoryReservationApplicationService service = new InventoryReservationApplicationService(
                repository,
                repository,
                operationLogService,
                new SequenceInventoryReservationNoGenerator());

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<InventoryReservationResultDTO> first = CompletableFuture.supplyAsync(() -> {
                await(start);
                return service.reserveStock(1001L, "ORDER-C1",
                        List.of(new InventoryReservationItemDTO(101L, 40)));
            }, pool);
            CompletableFuture<InventoryReservationResultDTO> second = CompletableFuture.supplyAsync(() -> {
                await(start);
                return service.reserveStock(1001L, "ORDER-C2",
                        List.of(new InventoryReservationItemDTO(101L, 40)));
            }, pool);

            start.countDown();

            InventoryReservationResultDTO firstResult = first.get(5, TimeUnit.SECONDS);
            InventoryReservationResultDTO secondResult = second.get(5, TimeUnit.SECONDS);

            assertEquals(InventoryReservation.STATUS_RESERVED, firstResult.getReservationStatus());
            assertEquals(InventoryReservation.STATUS_RESERVED, secondResult.getReservationStatus());

            Inventory inventory = repository.findInventory(1001L, 101L).orElseThrow();
            assertEquals(80, inventory.getReservedQuantity());
            assertEquals(20, inventory.getAvailableQuantity());
            assertTrue(inventory.getVersion() >= 2);

            assertNotNull(repository.findReservation(1001L, "ORDER-C1").orElse(null));
            assertNotNull(repository.findReservation(1001L, "ORDER-C2").orElse(null));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void shouldMoveOutboxToDeadLetterAfterRetryExhausted() {
        OptimisticInventoryRepository repository = new OptimisticInventoryRepository(true);
        InventoryAuditOutboxRetryService retryService = new InventoryAuditOutboxRetryService(repository);
        ReflectionTestUtils.setField(retryService, "enabled", true);
        ReflectionTestUtils.setField(retryService, "batchSize", 10);
        ReflectionTestUtils.setField(retryService, "maxRetries", 1);
        ReflectionTestUtils.setField(retryService, "baseDelaySeconds", 1L);
        ReflectionTestUtils.setField(retryService, "maxDelaySeconds", 10L);

        Instant now = Instant.parse("2026-03-26T10:00:00Z");
        repository.saveAuditOutbox(new InventoryAuditOutbox(null, 1001L, "ORDER-DEAD", "RSV-DEAD",
                InventoryAuditLog.ACTION_RESERVE, InventoryAuditLog.OPERATOR_TYPE_SYSTEM,
                InventoryAuditLog.OPERATOR_ID_SYSTEM, now, "INIT", InventoryAuditOutbox.STATUS_NEW,
                1, Instant.EPOCH, null, now, now));

        retryService.retryAuditOutbox();

        assertEquals(1, repository.deadLetterCount());
        assertTrue(repository.findRetryableAuditOutbox(Instant.now().plusSeconds(3600), 10).isEmpty());
    }

    @Test
    void shouldDeleteOutboxAfterRetrySuccess() {
        OptimisticInventoryRepository repository = new OptimisticInventoryRepository(false);
        InventoryAuditOutboxRetryService retryService = new InventoryAuditOutboxRetryService(repository);
        ReflectionTestUtils.setField(retryService, "enabled", true);
        ReflectionTestUtils.setField(retryService, "batchSize", 10);
        ReflectionTestUtils.setField(retryService, "maxRetries", 3);
        ReflectionTestUtils.setField(retryService, "baseDelaySeconds", 1L);
        ReflectionTestUtils.setField(retryService, "maxDelaySeconds", 10L);

        Instant now = Instant.parse("2026-03-26T10:00:00Z");
        repository.saveAuditOutbox(new InventoryAuditOutbox(null, 1001L, "ORDER-OK", "RSV-OK",
                InventoryAuditLog.ACTION_RESERVE, InventoryAuditLog.OPERATOR_TYPE_SYSTEM,
                InventoryAuditLog.OPERATOR_ID_SYSTEM, now, "INIT", InventoryAuditOutbox.STATUS_NEW,
                0, Instant.EPOCH, null, now, now));

        retryService.retryAuditOutbox();

        assertTrue(repository.findRetryableAuditOutbox(Instant.now().plusSeconds(3600), 10).isEmpty());
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

    private static final class OptimisticInventoryRepository implements InventoryStockRepository,
            InventoryReservationRepository, InventoryLogRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
        private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();
        private final Map<Long, InventoryAuditOutbox> outboxMap = new ConcurrentHashMap<>();
        private final Map<Long, InventoryAuditDeadLetter> deadLetterMap = new ConcurrentHashMap<>();
        private final AtomicLong outboxIdGenerator = new AtomicLong(1000L);
        private final AtomicLong deadLetterIdGenerator = new AtomicLong(2000L);
        private final boolean failAuditPersist;

        private OptimisticInventoryRepository(boolean failAuditPersist) {
            this.failAuditPersist = failAuditPersist;
            inventories.put(key(1001L, 101L), new Inventory(1L, 1001L, 101L, 1L,
                    100, 0, 100, Inventory.STATUS_ENABLED, 0L,
                    Instant.parse("2026-03-26T09:59:00Z")));
        }

        @Override
        public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
            return Optional.ofNullable(inventories.get(key(tenantId, skuId))).map(this::copy);
        }

        @Override
        public List<Inventory> findInventories(Long tenantId) {
            return inventories.values().stream()
                    .filter(item -> item.getTenantId().equals(tenantId))
                    .map(this::copy)
                    .toList();
        }

        @Override
        public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
            return skuIds.stream()
                    .map(skuId -> inventories.get(key(tenantId, skuId)))
                    .filter(java.util.Objects::nonNull)
                    .map(this::copy)
                    .toList();
        }

        @Override
        public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
            return findInventories(tenantId).stream()
                    .filter(item -> skuId == null || item.getSkuId().equals(skuId))
                    .filter(item -> status == null || status.equals(item.getStatus()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long countInventories(Long tenantId, Long skuId, String status) {
            return pageInventories(tenantId, skuId, status, 1, Integer.MAX_VALUE).size();
        }

        @Override
        public synchronized Inventory saveInventory(Inventory inventory) {
            Inventory current = inventories.get(key(inventory.getTenantId(), inventory.getSkuId()));
            if (current == null) {
                throw new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(inventory.getSkuId()));
            }
            if (!current.getVersion().equals(inventory.getVersion())) {
                throw new InventoryDomainException(InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED,
                        String.valueOf(inventory.getSkuId()));
            }
            Inventory persisted = copy(inventory);
            persisted.markPersisted(current.getVersion() + 1);
            inventories.put(key(persisted.getTenantId(), persisted.getSkuId()), persisted);
            return copy(persisted);
        }

        @Override
        public InventoryReservation saveReservation(InventoryReservation reservation) {
            String key = reservationKey(reservation.getTenantId(), reservation.getOrderNo());
            InventoryReservation existing = reservations.get(key);
            if (existing != null && !existing.getReservationNo().equals(reservation.getReservationNo())) {
                throw new DuplicateKeyException("duplicate orderNo");
            }
            reservations.put(key, reservation);
            return reservation;
        }

        @Override
        public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
            return Optional.ofNullable(reservations.get(reservationKey(tenantId, orderNo)));
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
            ledgers.computeIfAbsent(reservationKey(ledger.getTenantId(), ledger.getOrderNo()),
                            ignored -> new ArrayList<>())
                    .add(ledger);
        }

        @Override
        public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
            return List.copyOf(ledgers.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
            if (failAuditPersist) {
                throw new RuntimeException("force-fail-audit");
            }
            auditLogs.computeIfAbsent(reservationKey(auditLog.getTenantId(), auditLog.getOrderNo()),
                    ignored -> new ArrayList<>()).add(auditLog);
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.copyOf(auditLogs.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
        }

        @Override
        public void saveAuditOutbox(InventoryAuditOutbox outbox) {
            if (outbox.getId() == null) {
                outbox.setId(outboxIdGenerator.incrementAndGet());
            }
            outboxMap.put(outbox.getId(), outbox);
        }

        @Override
        public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
            return outboxMap.values().stream()
                    .filter(item -> InventoryAuditOutbox.STATUS_NEW.equals(item.getStatus())
                            || InventoryAuditOutbox.STATUS_RETRYING.equals(item.getStatus()))
                    .filter(item -> item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
                    .sorted(java.util.Comparator.comparing(InventoryAuditOutbox::getFailedAt).thenComparing(InventoryAuditOutbox::getId))
                    .limit(limit)
                    .toList();
        }

        @Override
        public void updateAuditOutboxForRetry(Long outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                              Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox != null) {
                outbox.setStatus(InventoryAuditOutbox.STATUS_RETRYING);
                outbox.setRetryCount(retryCount);
                outbox.setNextRetryAt(nextRetryAt);
                outbox.setErrorMessage(errorMessage);
                outbox.setUpdatedAt(updatedAt);
            }
        }

        @Override
        public void markAuditOutboxDead(Long outboxId, int retryCount, String deadReason, Instant updatedAt) {
            InventoryAuditOutbox outbox = outboxMap.get(outboxId);
            if (outbox != null) {
                outbox.setStatus(InventoryAuditOutbox.STATUS_DEAD);
                outbox.setRetryCount(retryCount);
                outbox.setDeadReason(deadReason);
                outbox.setUpdatedAt(updatedAt);
            }
        }

        @Override
        public void deleteAuditOutbox(Long outboxId) {
            outboxMap.remove(outboxId);
        }

        @Override
        public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
            if (deadLetter.getId() == null) {
                deadLetter.setId(deadLetterIdGenerator.incrementAndGet());
            }
            deadLetterMap.put(deadLetter.getId(), deadLetter);
        }

        private int deadLetterCount() {
            return deadLetterMap.size();
        }

        private Inventory copy(Inventory source) {
            return new Inventory(source.getId(), source.getTenantId(), source.getSkuId(), source.getWarehouseId(),
                    source.getOnHandQuantity(), source.getReservedQuantity(), source.getAvailableQuantity(),
                    source.getStatus(), source.getVersion(), source.getUpdatedAt());
        }

        private static String key(Long tenantId, Long skuId) {
            return tenantId + ":" + skuId;
        }

        private static String reservationKey(Long tenantId, String orderNo) {
            return tenantId + ":" + orderNo;
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
