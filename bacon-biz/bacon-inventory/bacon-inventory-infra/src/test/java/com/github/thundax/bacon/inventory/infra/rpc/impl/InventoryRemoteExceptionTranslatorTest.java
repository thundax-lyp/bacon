package com.github.thundax.bacon.inventory.infra.rpc.impl;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryRemoteExceptionTranslatorTest {

    @Test
    void shouldTranslateHttpStatusToDomainError() {
        RuntimeException translated = InventoryRemoteExceptionTranslator.translate("reserveStock",
                HttpClientErrorException.create(HttpStatusCode.valueOf(404), "not-found", HttpHeaders.EMPTY,
                        new byte[0], null));

        assertEquals(InventoryErrorCode.INVENTORY_REMOTE_NOT_FOUND.code(), ((InventoryDomainException) translated).getCode());
    }

    @Test
    void shouldTranslateCircuitOpenToDomainError() {
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("inventoryRemoteTest");
        RuntimeException translated = InventoryRemoteExceptionTranslator.translate("reserveStock",
                CallNotPermittedException.createCallNotPermittedException(circuitBreaker));

        assertEquals(InventoryErrorCode.INVENTORY_REMOTE_CIRCUIT_OPEN.code(), ((InventoryDomainException) translated).getCode());
    }

    @Test
    void shouldTranslateUnknownToUnavailable() {
        RuntimeException translated = InventoryRemoteExceptionTranslator.translate("reserveStock",
                new RuntimeException("boom"));

        assertEquals(InventoryErrorCode.INVENTORY_REMOTE_UNAVAILABLE.code(), ((InventoryDomainException) translated).getCode());
    }
}
