package com.github.thundax.bacon.product.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.product.domain.model.entity.ProductOutbox;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.model.enums.OutboxEventType;
import com.github.thundax.bacon.product.domain.model.enums.OutboxStatus;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductOutboxDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSkuDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSpuDO;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProductPersistenceAssemblerTest {

    private final ProductPersistenceAssembler assembler = new ProductPersistenceAssembler();

    @Test
    void shouldMapSpuBetweenDomainAndDataObject() {
        ProductSpu spu = ProductSpu.create(1L, 100L, "SPU-1", "Coffee", 10L, "desc", "obj-1");

        ProductSpuDO dataObject = assembler.toDataObject(spu, Instant.parse("2026-04-28T00:00:00Z"));
        ProductSpu restored = assembler.toSpu(dataObject);

        assertThat(dataObject.getTenantId()).isEqualTo(100L);
        assertThat(dataObject.getProductStatus()).isEqualTo("DRAFT");
        assertThat(restored.getSpuCode()).isEqualTo("SPU-1");
        assertThat(restored.getVersion()).isEqualTo(1L);
    }

    @Test
    void shouldMapSkuSalePriceAndSpecAttributes() {
        ProductSku sku = ProductSku.create(2L, 100L, 1L, "SKU-1", "Latte", "{\"size\":\"M\"}", BigDecimal.TEN);

        ProductSkuDO dataObject = assembler.toDataObject(sku, Instant.parse("2026-04-28T00:00:00Z"));
        ProductSku restored = assembler.toSku(dataObject);

        assertThat(dataObject.getSalePrice()).isEqualByComparingTo("10");
        assertThat(restored.getSpecAttributes()).isEqualTo("{\"size\":\"M\"}");
        assertThat(restored.isEnabled()).isTrue();
    }

    @Test
    void shouldMapOutboxRetryState() {
        ProductSpu spu = ProductSpu.create(1L, 100L, "SPU-1", "Coffee", 10L, null, null);
        ProductOutbox outbox = ProductOutbox.create(9L, spu, OutboxEventType.PRODUCT_CREATED, "{}");
        outbox.claim("worker-1", Instant.parse("2026-04-28T00:05:00Z"));
        outbox.fail(Instant.parse("2026-04-28T00:10:00Z"));

        ProductOutboxDO dataObject = assembler.toDataObject(outbox, Instant.parse("2026-04-28T00:00:00Z"));
        ProductOutbox restored = assembler.toOutbox(dataObject);

        assertThat(dataObject.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED.value());
        assertThat(restored.getRetryCount()).isEqualTo(1);
        assertThat(restored.getNextRetryAt()).isEqualTo("2026-04-28T00:10:00Z");
    }
}
