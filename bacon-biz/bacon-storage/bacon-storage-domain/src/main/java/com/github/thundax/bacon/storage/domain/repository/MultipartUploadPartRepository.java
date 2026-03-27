package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;

import java.util.List;

/**
 * 分段上传分段仓储。
 */
public interface MultipartUploadPartRepository {

    MultipartUploadPart save(MultipartUploadPart part);

    List<MultipartUploadPart> listByUploadId(String uploadId);

    void deleteByUploadId(String uploadId);
}
