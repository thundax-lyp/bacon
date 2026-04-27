package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {

    ProductCategory save(ProductCategory category);

    ProductCategory update(ProductCategory category);

    Optional<ProductCategory> findById(Long categoryId);

    Optional<ProductCategory> findByCode(String categoryCode);

    List<ProductCategory> listByParentId(Long parentId);
}
