package com.github.thundax.bacon.product.application.service;

import com.github.thundax.bacon.product.application.document.ProductSearchDocument;
import com.github.thundax.bacon.product.application.port.ProductSearchDocumentGateway;
import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.model.entity.ProductOutbox;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.repository.ProductCategoryRepository;
import com.github.thundax.bacon.product.domain.repository.ProductOutboxRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSkuRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSpuRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ProductIndexSyncApplicationService {

    private final ProductOutboxRepository productOutboxRepository;
    private final ProductSpuRepository productSpuRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSearchDocumentGateway productSearchDocumentGateway;

    public ProductIndexSyncApplicationService(
            ProductOutboxRepository productOutboxRepository,
            ProductSpuRepository productSpuRepository,
            ProductSkuRepository productSkuRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductSearchDocumentGateway productSearchDocumentGateway) {
        this.productOutboxRepository = productOutboxRepository;
        this.productSpuRepository = productSpuRepository;
        this.productSkuRepository = productSkuRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSearchDocumentGateway = productSearchDocumentGateway;
    }

    public void syncEvent(Long eventId) {
        ProductOutbox outbox = productOutboxRepository
                .findById(eventId)
                .orElseThrow(() -> new ProductDomainException(ProductErrorCode.INVALID_OUTBOX, String.valueOf(eventId)));
        Long currentVersion = productSearchDocumentGateway
                .findCurrentVersion(outbox.getTenantId(), outbox.getAggregateId())
                .orElse(0L);
        if (currentVersion > outbox.getProductVersion()) {
            outbox.succeed();
            productOutboxRepository.update(outbox);
            return;
        }
        ProductSearchDocument document = rebuildDocument(outbox.getAggregateId());
        productSearchDocumentGateway.saveIfNewer(document);
        outbox.succeed();
        productOutboxRepository.update(outbox);
    }

    private ProductSearchDocument rebuildDocument(Long spuId) {
        ProductSpu spu = productSpuRepository
                .findById(spuId)
                .orElseThrow(() -> new ProductDomainException(ProductErrorCode.PRODUCT_NOT_FOUND, String.valueOf(spuId)));
        ProductCategory category = productCategoryRepository
                .findById(spu.getCategoryId())
                .orElseThrow(() -> new ProductDomainException(
                        ProductErrorCode.CATEGORY_NOT_FOUND, String.valueOf(spu.getCategoryId())));
        List<ProductSku> skus = productSkuRepository.listBySpuId(spuId);
        BigDecimal minSalePrice = skus.stream()
                .map(ProductSku::getSalePrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal maxSalePrice = skus.stream()
                .map(ProductSku::getSalePrice)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        String specSummary = skus.stream()
                .map(ProductSku::getSpecAttributes)
                .filter(Objects::nonNull)
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        int enabledSkuCount = (int) skus.stream().filter(ProductSku::isEnabled).count();
        return new ProductSearchDocument(
                spu.getTenantId(),
                spu.getSpuId(),
                spu.getSpuCode(),
                spu.getSpuName(),
                spu.getCategoryId(),
                category.getCategoryName(),
                spu.getMainImageObjectId(),
                spu.getProductStatus(),
                minSalePrice,
                maxSalePrice,
                skus.size(),
                enabledSkuCount,
                specSummary,
                spu.getVersion());
    }
}
