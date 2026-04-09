package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.application.config.StorageDeletionRetryProperties;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 存储对象删除补偿重试服务。
 */
@Slf4j
@Service
public class StoredObjectDeletionRetryService {

    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StoredObjectDeletionTransactionService storedObjectDeletionTransactionService;
    private final StorageDeletionRetryProperties properties;

    public StoredObjectDeletionRetryService(
            StoredObjectRepository storedObjectRepository,
            StoredObjectStorageRepository storedObjectStorageRepository,
            StoredObjectDeletionTransactionService storedObjectDeletionTransactionService,
            StorageDeletionRetryProperties properties) {
        this.storedObjectRepository = storedObjectRepository;
        this.storedObjectStorageRepository = storedObjectStorageRepository;
        this.storedObjectDeletionTransactionService = storedObjectDeletionTransactionService;
        this.properties = properties;
    }

    public int retryDeletingObjects() {
        if (!properties.isEnabled()) {
            return 0;
        }
        List<StoredObject> deletingObjects = storedObjectRepository.listByObjectStatus(
                StoredObjectStatus.DELETING, Math.max(properties.getBatchSize(), 1));
        int completedCount = 0;
        for (StoredObject storedObject : deletingObjects) {
            try {
                storedObjectStorageRepository.delete(storedObject);
                storedObjectDeletionTransactionService.markDeleted(storedObject.getId());
                Metrics.counter("bacon.storage.deletion.retry.success.total").increment();
                completedCount++;
            } catch (RuntimeException ex) {
                Metrics.counter("bacon.storage.deletion.retry.fail.total").increment();
                log.warn(
                        "Stored object deletion retry failed, objectId={}, objectKey={}, storageType={}",
                        storedObject.getId(),
                        storedObject.getObjectKey(),
                        storedObject.getStorageType(),
                        ex);
            }
        }
        return completedCount;
    }
}
