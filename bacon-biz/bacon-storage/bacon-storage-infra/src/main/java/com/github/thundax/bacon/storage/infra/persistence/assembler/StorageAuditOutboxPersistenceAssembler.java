package com.github.thundax.bacon.storage.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditOutboxStatus;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditOutboxDO;
import java.util.Objects;

public final class StorageAuditOutboxPersistenceAssembler {

    private StorageAuditOutboxPersistenceAssembler() {}

    public static StorageAuditOutboxDO toDataObject(StorageAuditOutbox storageAuditOutbox) {
        if (storageAuditOutbox == null) {
            return null;
        }
        Objects.requireNonNull(storageAuditOutbox.getId(), "storageAuditOutbox.id must not be null");
        return new StorageAuditOutboxDO(
                storageAuditOutbox.getId(),
                BaconContextHolder.requireTenantId(),
                storageAuditOutbox.getObjectId() == null ? null : storageAuditOutbox.getObjectId().value(),
                storageAuditOutbox.getOwnerType(),
                storageAuditOutbox.getOwnerId(),
                storageAuditOutbox.getActionType() == null ? null : storageAuditOutbox.getActionType().value(),
                storageAuditOutbox.getBeforeStatus(),
                storageAuditOutbox.getAfterStatus(),
                storageAuditOutbox.getOperatorType(),
                storageAuditOutbox.getOperatorId(),
                storageAuditOutbox.getOccurredAt(),
                storageAuditOutbox.getErrorMessage(),
                storageAuditOutbox.getStatus() == null ? null : storageAuditOutbox.getStatus().value(),
                storageAuditOutbox.getRetryCount(),
                storageAuditOutbox.getNextRetryAt(),
                storageAuditOutbox.getUpdatedAt());
    }

    public static StorageAuditOutbox toDomain(StorageAuditOutboxDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return StorageAuditOutbox.reconstruct(
                dataObject.getId(),
                dataObject.getObjectId() == null ? null : StoredObjectId.of(dataObject.getObjectId()),
                dataObject.getOwnerType(),
                dataObject.getOwnerId(),
                dataObject.getActionType() == null
                        ? null
                        : StorageAuditActionType.from(dataObject.getActionType()),
                dataObject.getBeforeStatus(),
                dataObject.getAfterStatus(),
                dataObject.getOperatorType(),
                dataObject.getOperatorId(),
                dataObject.getOccurredAt(),
                dataObject.getErrorMessage(),
                dataObject.getStatus() == null ? null : StorageAuditOutboxStatus.from(dataObject.getStatus()),
                dataObject.getRetryCount(),
                dataObject.getNextRetryAt(),
                dataObject.getUpdatedAt());
    }
}
