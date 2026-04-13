package com.github.thundax.bacon.storage.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditLogDO;
import java.util.Objects;

public final class StorageAuditLogPersistenceAssembler {

    private StorageAuditLogPersistenceAssembler() {}

    public static StorageAuditLogDO toDataObject(StorageAuditLog storageAuditLog) {
        if (storageAuditLog == null) {
            return null;
        }
        Objects.requireNonNull(storageAuditLog.getId(), "storageAuditLog.id must not be null");
        return new StorageAuditLogDO(
                storageAuditLog.getId(),
                BaconContextHolder.requireTenantId(),
                storageAuditLog.getObjectId() == null ? null : storageAuditLog.getObjectId().value(),
                storageAuditLog.getOwnerType(),
                storageAuditLog.getOwnerId(),
                storageAuditLog.getActionType() == null ? null : storageAuditLog.getActionType().value(),
                storageAuditLog.getBeforeStatus(),
                storageAuditLog.getAfterStatus(),
                storageAuditLog.getOperatorType(),
                storageAuditLog.getOperatorId(),
                storageAuditLog.getOccurredAt());
    }

    public static StorageAuditLog toDomain(StorageAuditLogDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return StorageAuditLog.reconstruct(
                dataObject.getId(),
                dataObject.getObjectId() == null ? null : StoredObjectId.of(dataObject.getObjectId()),
                dataObject.getOwnerType(),
                dataObject.getOwnerId(),
                dataObject.getActionType() == null ? null : StorageAuditActionType.from(dataObject.getActionType()),
                dataObject.getBeforeStatus(),
                dataObject.getAfterStatus(),
                dataObject.getOperatorType(),
                dataObject.getOperatorId(),
                dataObject.getOccurredAt());
    }
}
