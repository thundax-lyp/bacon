package com.github.thundax.bacon.product.domain.gateway;

import com.github.thundax.bacon.product.domain.model.search.ProductSearchDocument;
import java.util.Optional;

public interface ProductSearchDocumentGateway {

    Optional<Long> findCurrentVersion(Long tenantId, Long spuId);

    void saveIfNewer(ProductSearchDocument document);
}
