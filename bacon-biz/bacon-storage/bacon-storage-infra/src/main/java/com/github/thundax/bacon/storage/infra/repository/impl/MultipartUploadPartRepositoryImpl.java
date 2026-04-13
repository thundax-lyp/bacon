package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.infra.persistence.assembler.MultipartUploadPartPersistenceAssembler;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.MultipartUploadPartDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.MultipartUploadPartMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MultipartUploadPartRepositoryImpl implements MultipartUploadPartRepository {

    private final MultipartUploadPartMapper multipartUploadPartMapper;

    public MultipartUploadPartRepositoryImpl(MultipartUploadPartMapper multipartUploadPartMapper) {
        this.multipartUploadPartMapper = multipartUploadPartMapper;
    }

    @Override
    public MultipartUploadPart insert(MultipartUploadPart part) {
        MultipartUploadPartDO dataObject = MultipartUploadPartPersistenceAssembler.toDataObject(part);
        multipartUploadPartMapper.insert(dataObject);
        return MultipartUploadPartPersistenceAssembler.toDomain(dataObject);
    }

    @Override
    public MultipartUploadPart update(MultipartUploadPart part) {
        MultipartUploadPartDO dataObject = MultipartUploadPartPersistenceAssembler.toDataObject(part);
        multipartUploadPartMapper.updateById(dataObject);
        return MultipartUploadPartPersistenceAssembler.toDomain(dataObject);
    }

    @Override
    public Optional<MultipartUploadPart> findByUploadIdAndPartNumber(String uploadId, Integer partNumber) {
        return Optional.ofNullable(multipartUploadPartMapper.selectOne(Wrappers.<MultipartUploadPartDO>lambdaQuery()
                        .eq(MultipartUploadPartDO::getUploadId, uploadId)
                        .eq(MultipartUploadPartDO::getPartNumber, partNumber)))
                .map(MultipartUploadPartPersistenceAssembler::toDomain);
    }

    @Override
    public List<MultipartUploadPart> listByUploadId(String uploadId) {
        return multipartUploadPartMapper
                .selectList(Wrappers.<MultipartUploadPartDO>lambdaQuery()
                        .eq(MultipartUploadPartDO::getUploadId, uploadId)
                        .orderByAsc(MultipartUploadPartDO::getPartNumber))
                .stream()
                .map(MultipartUploadPartPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public void deleteByUploadId(String uploadId) {
        multipartUploadPartMapper.delete(
                Wrappers.<MultipartUploadPartDO>lambdaQuery().eq(MultipartUploadPartDO::getUploadId, uploadId));
    }
}
