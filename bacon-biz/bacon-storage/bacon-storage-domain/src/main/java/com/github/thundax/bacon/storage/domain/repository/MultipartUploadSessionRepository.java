package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 分段上传会话仓储。
 */
public interface MultipartUploadSessionRepository {

    MultipartUploadSession save(MultipartUploadSession session);

    Optional<MultipartUploadSession> findByUploadId(String uploadId);

    List<MultipartUploadSession> listExpiredSessions(List<UploadStatus> uploadStatuses, Instant expireBefore, int limit);
}
