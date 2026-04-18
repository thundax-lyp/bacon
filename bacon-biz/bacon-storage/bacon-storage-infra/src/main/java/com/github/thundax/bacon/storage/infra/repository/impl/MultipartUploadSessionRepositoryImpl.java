package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.infra.persistence.assembler.MultipartUploadSessionPersistenceAssembler;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.MultipartUploadSessionDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.MultipartUploadSessionMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MultipartUploadSessionRepositoryImpl implements MultipartUploadSessionRepository {

    private final MultipartUploadSessionMapper multipartUploadSessionMapper;

    public MultipartUploadSessionRepositoryImpl(MultipartUploadSessionMapper multipartUploadSessionMapper) {
        this.multipartUploadSessionMapper = multipartUploadSessionMapper;
    }

    @Override
    public MultipartUploadSession insert(MultipartUploadSession session) {
        MultipartUploadSessionDO dataObject = MultipartUploadSessionPersistenceAssembler.toDataObject(session);
        multipartUploadSessionMapper.insert(dataObject);
        return MultipartUploadSessionPersistenceAssembler.toDomain(dataObject);
    }

    @Override
    public MultipartUploadSession update(MultipartUploadSession session) {
        MultipartUploadSessionDO dataObject = MultipartUploadSessionPersistenceAssembler.toDataObject(session);
        multipartUploadSessionMapper.updateById(dataObject);
        return MultipartUploadSessionPersistenceAssembler.toDomain(dataObject);
    }

    @Override
    public Optional<MultipartUploadSession> findByUploadId(String uploadId) {
        return Optional.ofNullable(
                        multipartUploadSessionMapper.selectOne(Wrappers.<MultipartUploadSessionDO>lambdaQuery()
                                .eq(MultipartUploadSessionDO::getUploadId, uploadId)))
                .map(MultipartUploadSessionPersistenceAssembler::toDomain);
    }

    @Override
    public List<MultipartUploadSession> listExpired(
            List<UploadStatus> uploadStatuses, Instant expireBefore, int limit) {
        return multipartUploadSessionMapper
                .selectList(Wrappers.<MultipartUploadSessionDO>lambdaQuery()
                        .in(MultipartUploadSessionDO::getUploadStatus, uploadStatuses)
                        .lt(MultipartUploadSessionDO::getUpdatedAt, expireBefore)
                        .orderByAsc(MultipartUploadSessionDO::getUpdatedAt)
                        .last("limit " + limit))
                .stream()
                .map(MultipartUploadSessionPersistenceAssembler::toDomain)
                .toList();
    }
}
