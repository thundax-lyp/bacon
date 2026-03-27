package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;

import java.util.Optional;

/**
 * 分段上传会话仓储。
 */
public interface MultipartUploadSessionRepository {

    MultipartUploadSession save(MultipartUploadSession session);

    Optional<MultipartUploadSession> findByUploadId(String uploadId);
}
