package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.MultipartUploadSessionDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.MultipartUploadSessionMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MultipartUploadSessionRepositoryImpl implements MultipartUploadSessionRepository {

    private static final String BIZ_TAG = "storage_multipart_upload";

    private final MultipartUploadSessionMapper multipartUploadSessionMapper;
    private final IdGenerator idGenerator;

    public MultipartUploadSessionRepositoryImpl(MultipartUploadSessionMapper multipartUploadSessionMapper,
                                                IdGenerator idGenerator) {
        this.multipartUploadSessionMapper = multipartUploadSessionMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public MultipartUploadSession save(MultipartUploadSession session) {
        MultipartUploadSessionDO dataObject = toDataObject(session);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(BIZ_TAG));
            multipartUploadSessionMapper.insert(dataObject);
        } else {
            multipartUploadSessionMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    @Override
    public Optional<MultipartUploadSession> findByUploadId(String uploadId) {
        return Optional.ofNullable(multipartUploadSessionMapper.selectOne(Wrappers.<MultipartUploadSessionDO>lambdaQuery()
                .eq(MultipartUploadSessionDO::getUploadId, uploadId))).map(this::toDomain);
    }

    private MultipartUploadSessionDO toDataObject(MultipartUploadSession session) {
        return new MultipartUploadSessionDO(session.getId(), session.getUploadId(), session.getTenantId(),
                session.getOwnerType(), session.getOwnerId(), session.getCategory(), session.getOriginalFilename(), session.getContentType(),
                session.getObjectKey(), session.getProviderUploadId(), session.getTotalSize(), session.getPartSize(),
                session.getUploadedPartCount(), session.getUploadStatus(), session.getCreatedAt(),
                session.getUpdatedAt(), session.getCompletedAt(), session.getAbortedAt());
    }

    private MultipartUploadSession toDomain(MultipartUploadSessionDO dataObject) {
        return new MultipartUploadSession(dataObject.getId(), dataObject.getUploadId(), dataObject.getTenantId(),
                dataObject.getOwnerType(), dataObject.getOwnerId(), dataObject.getCategory(), dataObject.getOriginalFilename(),
                dataObject.getContentType(), dataObject.getObjectKey(), dataObject.getProviderUploadId(),
                dataObject.getTotalSize(), dataObject.getPartSize(), dataObject.getUploadedPartCount(),
                dataObject.getUploadStatus(), dataObject.getCreatedAt(), dataObject.getUpdatedAt(),
                dataObject.getCompletedAt(), dataObject.getAbortedAt());
    }
}
