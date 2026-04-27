package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.repository.ProductSpuRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSpuDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductSpuMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductSpuRepositoryImpl implements ProductSpuRepository {

    private final ProductSpuMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductSpuRepositoryImpl(ProductSpuMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductSpu save(ProductSpu spu) {
        mapper.insert(assembler.toDataObject(spu, Instant.now()));
        return spu;
    }

    @Override
    public ProductSpu update(ProductSpu spu) {
        ProductSpuDO dataObject = assembler.toDataObject(spu, Instant.now());
        dataObject.setCreatedAt(null);
        mapper.updateById(dataObject);
        return spu;
    }

    @Override
    public Optional<ProductSpu> findById(Long spuId) {
        return Optional.ofNullable(mapper.selectById(spuId)).map(assembler::toSpu);
    }

    @Override
    public Optional<ProductSpu> findByCode(String spuCode) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ProductSpuDO>()
                        .eq(ProductSpuDO::getSpuCode, spuCode)
                        .eq(ProductSpuDO::getDeleted, false)))
                .map(assembler::toSpu);
    }
}
