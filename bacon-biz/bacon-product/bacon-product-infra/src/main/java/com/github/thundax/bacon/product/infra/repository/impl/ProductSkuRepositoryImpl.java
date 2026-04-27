package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.repository.ProductSkuRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSkuDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductSkuMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductSkuRepositoryImpl implements ProductSkuRepository {

    private final ProductSkuMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductSkuRepositoryImpl(ProductSkuMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductSku save(ProductSku sku) {
        mapper.insert(assembler.toDataObject(sku, Instant.now()));
        return sku;
    }

    @Override
    public ProductSku update(ProductSku sku) {
        ProductSkuDO dataObject = assembler.toDataObject(sku, Instant.now());
        dataObject.setCreatedAt(null);
        mapper.updateById(dataObject);
        return sku;
    }

    @Override
    public Optional<ProductSku> findById(Long skuId) {
        return Optional.ofNullable(mapper.selectById(skuId)).map(assembler::toSku);
    }

    @Override
    public Optional<ProductSku> findByCode(String skuCode) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ProductSkuDO>()
                        .eq(ProductSkuDO::getSkuCode, skuCode)
                        .eq(ProductSkuDO::getDeleted, false)))
                .map(assembler::toSku);
    }

    @Override
    public List<ProductSku> listBySpuId(Long spuId) {
        return mapper.selectList(new LambdaQueryWrapper<ProductSkuDO>()
                        .eq(ProductSkuDO::getSpuId, spuId)
                        .eq(ProductSkuDO::getDeleted, false))
                .stream()
                .map(assembler::toSku)
                .toList();
    }
}
