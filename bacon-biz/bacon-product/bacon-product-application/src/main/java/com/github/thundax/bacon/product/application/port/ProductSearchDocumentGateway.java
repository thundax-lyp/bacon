package com.github.thundax.bacon.product.application.port;

import com.github.thundax.bacon.product.application.document.ProductSearchDocument;
import java.util.Optional;

public interface ProductSearchDocumentGateway {

    Optional<Long> findCurrentVersion(Long tenantId, Long spuId);

    void saveIfNewer(ProductSearchDocument document);
}
