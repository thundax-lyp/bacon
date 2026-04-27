package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductIdempotencyRecord;
import com.github.thundax.bacon.product.domain.repository.ProductIdempotencyRecordRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductIdempotencyRecordDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductIdempotencyRecordMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductIdempotencyRecordRepositoryImpl implements ProductIdempotencyRecordRepository {

    private final ProductIdempotencyRecordMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductIdempotencyRecordRepositoryImpl(
            ProductIdempotencyRecordMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductIdempotencyRecord insert(ProductIdempotencyRecord record) {
        mapper.insert(assembler.toDataObject(record, Instant.now()));
        return record;
    }

    @Override
    public ProductIdempotencyRecord update(ProductIdempotencyRecord record) {
        ProductIdempotencyRecordDO dataObject = assembler.toDataObject(record, Instant.now());
        dataObject.setCreatedAt(null);
        mapper.updateById(dataObject);
        return record;
    }

    @Override
    public Optional<ProductIdempotencyRecord> findByOperationTypeAndKey(
            String operationType, String idempotencyKey) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ProductIdempotencyRecordDO>()
                        .eq(ProductIdempotencyRecordDO::getOperationType, operationType)
                        .eq(ProductIdempotencyRecordDO::getIdempotencyKey, idempotencyKey)))
                .map(assembler::toIdempotencyRecord);
    }
}
