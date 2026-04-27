package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.repository.ProductCategoryRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductCategoryDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductCategoryMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final ProductCategoryMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductCategoryRepositoryImpl(ProductCategoryMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductCategory save(ProductCategory category) {
        mapper.insert(assembler.toDataObject(category, Instant.now()));
        return category;
    }

    @Override
    public ProductCategory update(ProductCategory category) {
        ProductCategoryDO dataObject = assembler.toDataObject(category, Instant.now());
        dataObject.setCreatedAt(null);
        mapper.updateById(dataObject);
        return category;
    }

    @Override
    public Optional<ProductCategory> findById(Long categoryId) {
        return Optional.ofNullable(mapper.selectById(categoryId)).map(assembler::toCategory);
    }

    @Override
    public Optional<ProductCategory> findByCode(String categoryCode) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ProductCategoryDO>()
                        .eq(ProductCategoryDO::getCategoryCode, categoryCode)
                        .eq(ProductCategoryDO::getDeleted, false)))
                .map(assembler::toCategory);
    }

    @Override
    public List<ProductCategory> listByParentId(Long parentId) {
        return mapper.selectList(new LambdaQueryWrapper<ProductCategoryDO>()
                        .eq(ProductCategoryDO::getParentId, parentId)
                        .eq(ProductCategoryDO::getDeleted, false)
                        .orderByAsc(ProductCategoryDO::getSortOrder))
                .stream()
                .map(assembler::toCategory)
                .toList();
    }
}
