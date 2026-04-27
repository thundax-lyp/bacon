package com.github.thundax.bacon.product.application.service;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.repository.ProductCategoryRepository;

public class ProductCategoryApplicationService {

    private static final String CATEGORY_ID_BIZ_TAG = "product-category-id";

    private final ProductCategoryRepository productCategoryRepository;
    private final IdGenerator idGenerator;

    public ProductCategoryApplicationService(ProductCategoryRepository productCategoryRepository, IdGenerator idGenerator) {
        this.productCategoryRepository = productCategoryRepository;
        this.idGenerator = idGenerator;
    }

    public ProductCategory createCategory(Long tenantId, Long parentId, String categoryCode, String categoryName, Integer sortOrder) {
        return productCategoryRepository.save(ProductCategory.create(
                idGenerator.nextId(CATEGORY_ID_BIZ_TAG), tenantId, parentId, categoryCode, categoryName, sortOrder));
    }
}
