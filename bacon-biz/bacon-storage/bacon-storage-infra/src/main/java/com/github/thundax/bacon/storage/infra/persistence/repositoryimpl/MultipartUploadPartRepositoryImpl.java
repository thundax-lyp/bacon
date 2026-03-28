package com.github.thundax.bacon.storage.infra.persistence.repositoryimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.MultipartUploadPartDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.MultipartUploadPartMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MultipartUploadPartRepositoryImpl implements MultipartUploadPartRepository {

    private static final String BIZ_TAG = "storage_multipart_upload_part";

    private final MultipartUploadPartMapper multipartUploadPartMapper;
    private final IdGenerator idGenerator;

    public MultipartUploadPartRepositoryImpl(MultipartUploadPartMapper multipartUploadPartMapper,
                                             IdGenerator idGenerator) {
        this.multipartUploadPartMapper = multipartUploadPartMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public MultipartUploadPart save(MultipartUploadPart part) {
        MultipartUploadPartDO dataObject = toDataObject(part);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(BIZ_TAG));
            multipartUploadPartMapper.insert(dataObject);
        } else {
            multipartUploadPartMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    @Override
    public Optional<MultipartUploadPart> findByUploadIdAndPartNumber(String uploadId, Integer partNumber) {
        return Optional.ofNullable(multipartUploadPartMapper.selectOne(Wrappers.<MultipartUploadPartDO>lambdaQuery()
                .eq(MultipartUploadPartDO::getUploadId, uploadId)
                .eq(MultipartUploadPartDO::getPartNumber, partNumber))).map(this::toDomain);
    }

    @Override
    public List<MultipartUploadPart> listByUploadId(String uploadId) {
        return multipartUploadPartMapper.selectList(Wrappers.<MultipartUploadPartDO>lambdaQuery()
                        .eq(MultipartUploadPartDO::getUploadId, uploadId)
                        .orderByAsc(MultipartUploadPartDO::getPartNumber))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByUploadId(String uploadId) {
        multipartUploadPartMapper.delete(Wrappers.<MultipartUploadPartDO>lambdaQuery()
                .eq(MultipartUploadPartDO::getUploadId, uploadId));
    }

    private MultipartUploadPartDO toDataObject(MultipartUploadPart part) {
        return new MultipartUploadPartDO(part.getId(), part.getUploadId(), part.getPartNumber(), part.getEtag(),
                part.getSize(), part.getCreatedAt());
    }

    private MultipartUploadPart toDomain(MultipartUploadPartDO dataObject) {
        return new MultipartUploadPart(dataObject.getId(), dataObject.getUploadId(), dataObject.getPartNumber(),
                dataObject.getEtag(), dataObject.getSize(), dataObject.getCreatedAt());
    }
}
