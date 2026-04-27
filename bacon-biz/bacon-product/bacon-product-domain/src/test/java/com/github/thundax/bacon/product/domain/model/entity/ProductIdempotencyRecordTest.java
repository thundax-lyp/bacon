package com.github.thundax.bacon.product.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.model.enums.IdempotencyStatus;
import org.junit.jupiter.api.Test;

class ProductIdempotencyRecordTest {

    @Test
    void shouldRejectDifferentRequestHashForSameIdempotencyKey() {
        ProductIdempotencyRecord record =
                ProductIdempotencyRecord.processing(1L, 10L, "CREATE_PRODUCT", "idem-1", "hash-a");

        assertThrows(ProductDomainException.class, () -> record.ensureSameRequest("hash-b"));
    }

    @Test
    void shouldMarkSuccessWithResultReference() {
        ProductIdempotencyRecord record =
                ProductIdempotencyRecord.processing(1L, 10L, "CREATE_PRODUCT", "idem-1", "hash-a");

        record.succeed("PRODUCT", "100", "{\"spuId\":100}");

        assertEquals(IdempotencyStatus.SUCCESS, record.getIdempotencyStatus());
        assertEquals("PRODUCT", record.getResultRefType());
        assertEquals("100", record.getResultRefId());
    }
}
