package com.github.thundax.bacon.inventory.infra.facade.impl;

import com.github.thundax.bacon.common.core.exception.BaconException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.RestClientResponseException;

final class InventoryRemoteExceptionTranslator {

    private InventoryRemoteExceptionTranslator() {
    }

    static RuntimeException translate(String operation, Throwable throwable) {
        if (throwable instanceof BaconException baconException) {
            return baconException;
        }
        if (throwable instanceof CallNotPermittedException) {
            return new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_CIRCUIT_OPEN, operation);
        }
        if (throwable.getClass().getSimpleName().contains("Bulkhead")) {
            return new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_BULKHEAD_FULL, operation);
        }
        if (throwable instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            return switch (statusCode) {
                case 400 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_BAD_REQUEST, operation);
                case 401 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_UNAUTHORIZED, operation);
                case 403 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_FORBIDDEN, operation);
                case 404 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_NOT_FOUND, operation);
                case 409 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_CONFLICT, operation);
                default -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_ERROR,
                        operation + ", status=" + statusCode);
            };
        }
        return new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_UNAVAILABLE, operation);
    }
}
