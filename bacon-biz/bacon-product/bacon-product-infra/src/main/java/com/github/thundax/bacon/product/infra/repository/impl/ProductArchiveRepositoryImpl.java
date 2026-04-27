package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductArchive;
import com.github.thundax.bacon.product.domain.repository.ProductArchiveRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductArchiveDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductArchiveMapper;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductArchiveRepositoryImpl implements ProductArchiveRepository {

    private final ProductArchiveMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductArchiveRepositoryImpl(ProductArchiveMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductArchive save(ProductArchive archive) {
        mapper.insert(assembler.toDataObject(archive));
        return archive;
    }

    @Override
    public Optional<ProductArchive> findBySpuIdAndVersion(Long spuId, Long productVersion) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ProductArchiveDO>()
                        .eq(ProductArchiveDO::getSpuId, spuId)
                        .eq(ProductArchiveDO::getProductVersion, productVersion)))
                .map(assembler::toArchive);
    }
}
