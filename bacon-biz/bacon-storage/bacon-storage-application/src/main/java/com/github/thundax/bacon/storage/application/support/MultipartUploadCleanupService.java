package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.application.config.StorageMultipartCleanupProperties;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 分片上传超时清理服务。
 */
@Slf4j
@Service
public class MultipartUploadCleanupService {

    private static final List<UploadStatus> EXPIRED_UPLOAD_STATUSES =
            List.of(UploadStatus.INITIATED, UploadStatus.UPLOADING, UploadStatus.ABORTED);

    private final MultipartUploadSessionRepository multipartUploadSessionRepository;
    private final MultipartUploadPartRepository multipartUploadPartRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StorageMultipartCleanupProperties properties;

    public MultipartUploadCleanupService(
            MultipartUploadSessionRepository multipartUploadSessionRepository,
            MultipartUploadPartRepository multipartUploadPartRepository,
            StoredObjectStorageRepository storedObjectStorageRepository,
            StorageMultipartCleanupProperties properties) {
        this.multipartUploadSessionRepository = multipartUploadSessionRepository;
        this.multipartUploadPartRepository = multipartUploadPartRepository;
        this.storedObjectStorageRepository = storedObjectStorageRepository;
        this.properties = properties;
    }

    public int cleanupExpiredSessions() {
        if (!properties.isEnabled()) {
            return 0;
        }
        Instant expireBefore = Instant.now().minusSeconds(properties.getTimeoutSeconds());
        List<MultipartUploadSession> expiredSessions = multipartUploadSessionRepository.listExpired(
                EXPIRED_UPLOAD_STATUSES, expireBefore, properties.getBatchSize());
        int cleanedCount = 0;
        for (MultipartUploadSession session : expiredSessions) {
            String uploadStatus = session.getUploadStatus().value();
            try {
                cleanupSingleSession(session);
                Metrics.counter("bacon.storage.multipart.cleanup.success.total", "uploadStatus", uploadStatus)
                        .increment();
                cleanedCount++;
            } catch (RuntimeException ex) {
                Metrics.counter("bacon.storage.multipart.cleanup.fail.total", "uploadStatus", uploadStatus)
                        .increment();
                log.warn(
                        "Multipart upload expired cleanup failed, uploadId={}, ownerType={}, ownerId={}",
                        session.getUploadId(),
                        session.getOwnerType(),
                        session.getOwnerId(),
                        ex);
            }
        }
        if (!expiredSessions.isEmpty()) {
            log.info(
                    "Multipart upload expired cleanup batch scanned, candidateCount={}, cleanedCount={}",
                    expiredSessions.size(),
                    cleanedCount);
        }
        return cleanedCount;
    }

    protected void cleanupSingleSession(MultipartUploadSession session) {
        storedObjectStorageRepository.delete(session);
        multipartUploadPartRepository.deleteByUploadId(session.getUploadId());
        if (!session.isAborted()) {
            session.markAborted();
            multipartUploadSessionRepository.update(session);
        }
        log.info(
                "Multipart upload expired cleanup completed, uploadId={}, ownerType={}, ownerId={}",
                session.getUploadId(),
                session.getOwnerType(),
                session.getOwnerId());
    }
}
