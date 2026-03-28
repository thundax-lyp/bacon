package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.application.config.StorageMultipartCleanupProperties;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 分片上传超时清理服务。
 */
@Slf4j
@Service
public class MultipartUploadCleanupService {

    private static final List<String> EXPIRED_UPLOAD_STATUSES = List.of(
            MultipartUploadSession.STATUS_INITIATED,
            MultipartUploadSession.STATUS_UPLOADING,
            MultipartUploadSession.STATUS_ABORTED
    );

    private final MultipartUploadSessionRepository multipartUploadSessionRepository;
    private final MultipartUploadPartRepository multipartUploadPartRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StorageMultipartCleanupProperties properties;

    public MultipartUploadCleanupService(MultipartUploadSessionRepository multipartUploadSessionRepository,
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
        List<MultipartUploadSession> expiredSessions = multipartUploadSessionRepository.listExpiredSessions(
                EXPIRED_UPLOAD_STATUSES, expireBefore, properties.getBatchSize());
        int cleanedCount = 0;
        for (MultipartUploadSession session : expiredSessions) {
            try {
                cleanupSingleSession(session);
                cleanedCount++;
            } catch (RuntimeException ex) {
                log.warn("Multipart upload expired cleanup failed, uploadId={}, ownerType={}, ownerId={}",
                        session.getUploadId(), session.getOwnerType(), session.getOwnerId(), ex);
            }
        }
        return cleanedCount;
    }

    protected void cleanupSingleSession(MultipartUploadSession session) {
        storedObjectStorageRepository.abortMultipartUpload(session);
        multipartUploadPartRepository.deleteByUploadId(session.getUploadId());
        if (!session.isAborted()) {
            session.markAborted();
            multipartUploadSessionRepository.save(session);
        }
        log.info("Multipart upload expired cleanup completed, uploadId={}, ownerType={}, ownerId={}",
                session.getUploadId(), session.getOwnerType(), session.getOwnerId());
    }
}
