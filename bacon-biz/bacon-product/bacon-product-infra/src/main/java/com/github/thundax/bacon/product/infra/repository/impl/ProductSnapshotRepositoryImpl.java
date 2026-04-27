package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductSnapshot;
import com.github.thundax.bacon.product.domain.repository.ProductSnapshotRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSnapshotDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductSnapshotMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductSnapshotRepositoryImpl implements ProductSnapshotRepository {

    private final ProductSnapshotMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductSnapshotRepositoryImpl(ProductSnapshotMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductSnapshot save(ProductSnapshot snapshot) {
        mapper.insert(assembler.toDataObject(snapshot, Instant.now()));
        return snapshot;
    }

    @Override
    public Optional<ProductSnapshot> findByOrderItem(String orderNo, String orderItemNo) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ProductSnapshotDO>()
                        .eq(ProductSnapshotDO::getOrderNo, orderNo)
                        .eq(ProductSnapshotDO::getOrderItemNo, orderItemNo)))
                .map(assembler::toSnapshot);
    }
}
