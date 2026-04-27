package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.enums.OutboxEventType;
import com.github.thundax.bacon.product.domain.model.enums.OutboxStatus;
import java.time.Instant;

public class ProductOutbox {

    private static final String AGGREGATE_TYPE_PRODUCT = "PRODUCT";

    private final Long eventId;
    private final Long tenantId;
    private final Long aggregateId;
    private final String aggregateType;
    private final OutboxEventType eventType;
    private final Long productVersion;
    private final String payload;
    private OutboxStatus outboxStatus;
    private Integer retryCount;
    private Instant nextRetryAt;
    private String processingOwner;
    private Instant leaseUntil;

    private ProductOutbox(
            Long eventId,
            Long tenantId,
            Long aggregateId,
            String aggregateType,
            OutboxEventType eventType,
            Long productVersion,
            String payload,
            OutboxStatus outboxStatus,
            Integer retryCount,
            Instant nextRetryAt,
            String processingOwner,
            Instant leaseUntil) {
        this.eventId = requireId(eventId, "eventId");
        this.tenantId = requireId(tenantId, "tenantId");
        this.aggregateId = requireId(aggregateId, "aggregateId");
        this.aggregateType = requireText(aggregateType, "aggregateType");
        this.eventType = requireEventType(eventType);
        this.productVersion = requireId(productVersion, "productVersion");
        this.payload = requireText(payload, "payload");
        this.outboxStatus = requireStatus(outboxStatus);
        this.retryCount = retryCount == null ? 0 : retryCount;
        this.nextRetryAt = nextRetryAt;
        this.processingOwner = processingOwner;
        this.leaseUntil = leaseUntil;
    }

    public static ProductOutbox create(Long eventId, ProductSpu spu, OutboxEventType eventType, String payload) {
        if (spu == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_OUTBOX, "spu");
        }
        return new ProductOutbox(
                eventId,
                spu.getTenantId(),
                spu.getSpuId(),
                AGGREGATE_TYPE_PRODUCT,
                eventType,
                spu.getVersion(),
                payload,
                OutboxStatus.PENDING,
                0,
                null,
                null,
                null);
    }

    public static ProductOutbox reconstruct(
            Long eventId,
            Long tenantId,
            Long aggregateId,
            String aggregateType,
            OutboxEventType eventType,
            Long productVersion,
            String payload,
            OutboxStatus outboxStatus,
            Integer retryCount,
            Instant nextRetryAt,
            String processingOwner,
            Instant leaseUntil) {
        return new ProductOutbox(
                eventId,
                tenantId,
                aggregateId,
                aggregateType,
                eventType,
                productVersion,
                payload,
                outboxStatus,
                retryCount,
                nextRetryAt,
                processingOwner,
                leaseUntil);
    }

    public void claim(String processingOwner, Instant leaseUntil) {
        if (processingOwner == null || processingOwner.isBlank() || leaseUntil == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_OUTBOX, "claim");
        }
        this.outboxStatus = OutboxStatus.PROCESSING;
        this.processingOwner = processingOwner;
        this.leaseUntil = leaseUntil;
    }

    public void succeed() {
        this.outboxStatus = OutboxStatus.SUCCEEDED;
        this.processingOwner = null;
        this.leaseUntil = null;
    }

    public void fail(Instant nextRetryAt) {
        this.outboxStatus = OutboxStatus.FAILED;
        this.retryCount = this.retryCount + 1;
        this.nextRetryAt = nextRetryAt;
        this.processingOwner = null;
        this.leaseUntil = null;
    }

    public void markDead() {
        this.outboxStatus = OutboxStatus.DEAD;
        this.processingOwner = null;
        this.leaseUntil = null;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public OutboxEventType getEventType() {
        return eventType;
    }

    public Long getProductVersion() {
        return productVersion;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getOutboxStatus() {
        return outboxStatus;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public String getProcessingOwner() {
        return processingOwner;
    }

    public Instant getLeaseUntil() {
        return leaseUntil;
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_OUTBOX, field);
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_OUTBOX, field);
        }
        return value;
    }

    private static OutboxEventType requireEventType(OutboxEventType eventType) {
        if (eventType == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_OUTBOX, "eventType");
        }
        return eventType;
    }

    private static OutboxStatus requireStatus(OutboxStatus status) {
        if (status == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_OUTBOX, "outboxStatus");
        }
        return status;
    }
}
