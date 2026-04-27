package com.github.thundax.bacon.product.infra.search;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.product.application.document.ProductSearchDocument;
import com.github.thundax.bacon.product.application.port.ProductSearchDocumentGateway;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.product.elasticsearch.enabled", havingValue = "true")
public class ProductSearchDocumentElasticsearchGateway implements ProductSearchDocumentGateway {

    private static final String INDEX_NAME = "bacon_product_spu";

    private final RestClient restClient;

    public ProductSearchDocumentElasticsearchGateway(
            RestClientFactory restClientFactory,
            @Value("${bacon.product.elasticsearch.base-url:http://localhost:9200}") String baseUrl) {
        this.restClient = restClientFactory.create(baseUrl);
    }

    @Override
    public Optional<Long> findCurrentVersion(Long tenantId, Long spuId) {
        Map<?, ?> response = restClient
                .get()
                .uri("/{index}/_doc/{id}?_source_includes=productVersion", INDEX_NAME, documentId(tenantId, spuId))
                .retrieve()
                .body(Map.class);
        if (response == null || !(response.get("_source") instanceof Map<?, ?> source)) {
            return Optional.empty();
        }
        Object productVersion = source.get("productVersion");
        if (productVersion instanceof Number number) {
            return Optional.of(number.longValue());
        }
        return Optional.empty();
    }

    @Override
    public void saveIfNewer(ProductSearchDocument document) {
        Long currentVersion = findCurrentVersion(document.tenantId(), document.spuId()).orElse(0L);
        if (currentVersion > document.productVersion()) {
            return;
        }
        restClient
                .put()
                .uri("/{index}/_doc/{id}", INDEX_NAME, documentId(document.tenantId(), document.spuId()))
                .body(ProductSearchDocumentPayload.from(document))
                .retrieve()
                .toBodilessEntity();
    }

    private String documentId(Long tenantId, Long spuId) {
        return tenantId + ":" + spuId;
    }

}
