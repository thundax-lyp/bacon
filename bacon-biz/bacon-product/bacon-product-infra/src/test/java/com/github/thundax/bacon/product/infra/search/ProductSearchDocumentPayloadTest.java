package com.github.thundax.bacon.product.infra.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.product.application.document.ProductSearchDocument;
import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductSearchDocumentPayloadTest {

    @Test
    void shouldBuildSearchProjectionPayloadFromApplicationDocument() {
        ProductSearchDocument document = new ProductSearchDocument(
                100L,
                1L,
                "SPU-1",
                "Coffee",
                10L,
                "Drink",
                "obj-1",
                ProductStatus.ON_SALE,
                BigDecimal.ONE,
                BigDecimal.TEN,
                2,
                1,
                "{\"size\":\"M\"}",
                3L);

        ProductSearchDocumentPayload payload = ProductSearchDocumentPayload.from(document);

        assertThat(payload.productStatus()).isEqualTo("ON_SALE");
        assertThat(payload.searchText()).contains("Coffee", "Drink");
        assertThat(payload.productVersion()).isEqualTo(3L);
        assertThat(payload.indexedAt()).isNotNull();
    }
}
