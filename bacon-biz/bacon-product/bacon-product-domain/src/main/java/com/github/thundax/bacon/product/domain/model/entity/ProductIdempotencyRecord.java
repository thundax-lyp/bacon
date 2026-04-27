package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.enums.IdempotencyStatus;

public class ProductIdempotencyRecord {

    private final Long idempotencyId;
    private final Long tenantId;
    private final String operationType;
    private final String idempotencyKey;
    private final String requestHash;
    private String resultRefType;
    private String resultRefId;
    private String resultPayload;
    private IdempotencyStatus idempotencyStatus;

    private ProductIdempotencyRecord(
            Long idempotencyId,
            Long tenantId,
            String operationType,
            String idempotencyKey,
            String requestHash,
            String resultRefType,
            String resultRefId,
            String resultPayload,
            IdempotencyStatus idempotencyStatus) {
        this.idempotencyId = requireId(idempotencyId, "idempotencyId");
        this.tenantId = requireId(tenantId, "tenantId");
        this.operationType = requireText(operationType, "operationType");
        this.idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        this.requestHash = requireText(requestHash, "requestHash");
        this.resultRefType = resultRefType;
        this.resultRefId = resultRefId;
        this.resultPayload = resultPayload;
        this.idempotencyStatus = requireStatus(idempotencyStatus);
    }

    public static ProductIdempotencyRecord processing(
            Long idempotencyId, Long tenantId, String operationType, String idempotencyKey, String requestHash) {
        return new ProductIdempotencyRecord(
                idempotencyId, tenantId, operationType, idempotencyKey, requestHash, null, null, null,
                IdempotencyStatus.PROCESSING);
    }

    public static ProductIdempotencyRecord reconstruct(
            Long idempotencyId,
            Long tenantId,
            String operationType,
            String idempotencyKey,
            String requestHash,
            String resultRefType,
            String resultRefId,
            String resultPayload,
            IdempotencyStatus idempotencyStatus) {
        return new ProductIdempotencyRecord(
                idempotencyId,
                tenantId,
                operationType,
                idempotencyKey,
                requestHash,
                resultRefType,
                resultRefId,
                resultPayload,
                idempotencyStatus);
    }

    public void ensureSameRequest(String requestHash) {
        if (!this.requestHash.equals(requestHash)) {
            throw new ProductDomainException(ProductErrorCode.IDEMPOTENCY_KEY_CONFLICT, idempotencyKey);
        }
    }

    public void succeed(String resultRefType, String resultRefId, String resultPayload) {
        this.resultRefType = requireText(resultRefType, "resultRefType");
        this.resultRefId = requireText(resultRefId, "resultRefId");
        this.resultPayload = resultPayload;
        this.idempotencyStatus = IdempotencyStatus.SUCCESS;
    }

    public void fail(String failurePayload) {
        this.resultPayload = failurePayload;
        this.idempotencyStatus = IdempotencyStatus.FAILED;
    }

    public Long getIdempotencyId() {
        return idempotencyId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getResultRefType() {
        return resultRefType;
    }

    public String getResultRefId() {
        return resultRefId;
    }

    public String getResultPayload() {
        return resultPayload;
    }

    public IdempotencyStatus getIdempotencyStatus() {
        return idempotencyStatus;
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_IDEMPOTENCY_RECORD, field);
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_IDEMPOTENCY_RECORD, field);
        }
        return value;
    }

    private static IdempotencyStatus requireStatus(IdempotencyStatus status) {
        if (status == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_IDEMPOTENCY_RECORD, "idempotencyStatus");
        }
        return status;
    }
}
