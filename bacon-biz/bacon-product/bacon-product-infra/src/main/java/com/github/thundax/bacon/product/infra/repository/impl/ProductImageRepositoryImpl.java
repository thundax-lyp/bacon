package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductImage;
import com.github.thundax.bacon.product.domain.repository.ProductImageRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductImageDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductImageMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final ProductImageMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductImageRepositoryImpl(ProductImageMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductImage save(ProductImage image) {
        mapper.insert(assembler.toDataObject(image, Instant.now()));
        return image;
    }

    @Override
    public List<ProductImage> listBySpuId(Long spuId) {
        return mapper.selectList(new LambdaQueryWrapper<ProductImageDO>()
                        .eq(ProductImageDO::getSpuId, spuId)
                        .eq(ProductImageDO::getDeleted, false)
                        .orderByAsc(ProductImageDO::getSortOrder))
                .stream()
                .map(assembler::toImage)
                .toList();
    }
}
