package com.github.thundax.bacon.order.application.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrderIdempotencyRecoveryRetrierTest {

    private static final TenantId TENANT_ID = TenantId.of(1000001L);

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void scheduledRecoveryShouldRestoreRecordTenantContext() {
        Instant now = Instant.now();
        TenantAwareOrderIdempotencyRepository repository = new TenantAwareOrderIdempotencyRepository(
                OrderIdempotencyRecord.reconstruct(
                        OrderIdempotencyRecordKey.of(OrderNo.of("ORD-SCHEDULED-1"), "MARK_PAID"),
                        OrderIdempotencyStatus.PROCESSING,
                        1,
                        null,
                        "stale-owner",
                        now.minusSeconds(1),
                        now.minusSeconds(30),
                        now.minusSeconds(60),
                        now.minusSeconds(30)));
        OrderIdempotencyRecoveryRetrier retrier = new OrderIdempotencyRecoveryRetrier(repository);
        ReflectionTestUtils.setField(retrier, "enabled", true);

        retrier.recoverExpired();

        assertEquals(OrderIdempotencyStatus.FAILED, repository.record.getStatus());
        assertEquals(TENANT_ID.value(), repository.updatedTenantId);
        assertNull(BaconContextHolder.currentTenantId());
    }

    private static final class TenantAwareOrderIdempotencyRepository implements OrderIdempotencyRepository {

        private OrderIdempotencyRecord record;
        private Long updatedTenantId;

        private TenantAwareOrderIdempotencyRepository(OrderIdempotencyRecord record) {
            this.record = record;
        }

        @Override
        public boolean updateStatus(
                OrderIdempotencyRecord record,
                OrderIdempotencyStatus currentStatus,
                Instant leaseExpiredBefore) {
            if (BaconContextHolder.currentTenantId() == null) {
                throw new IllegalStateException("tenantId must not be null");
            }
            if (this.record.getStatus() != currentStatus) {
                return false;
            }
            updatedTenantId = BaconContextHolder.currentTenantId();
            this.record = record;
            return true;
        }

        @Override
        public List<TenantScopedIdempotencyRecord> listExpiredProcessingAcrossTenants(Instant now) {
            return List.of(new TenantScopedIdempotencyRecord(TENANT_ID, copy(record)));
        }

        private OrderIdempotencyRecord copy(OrderIdempotencyRecord source) {
            return OrderIdempotencyRecord.reconstruct(
                    source.getKey(),
                    source.getStatus(),
                    source.getAttemptCount(),
                    source.getLastError(),
                    source.getProcessingOwner(),
                    source.getLeaseUntil(),
                    source.getClaimedAt(),
                    source.getCreatedAt(),
                    source.getUpdatedAt());
        }
    }
}
