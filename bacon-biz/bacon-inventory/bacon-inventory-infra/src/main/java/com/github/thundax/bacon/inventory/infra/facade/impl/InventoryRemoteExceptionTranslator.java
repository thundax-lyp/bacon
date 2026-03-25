package com.github.thundax.bacon.inventory.infra.facade.impl;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.BaconException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.ForbiddenException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.exception.SystemException;
import com.github.thundax.bacon.common.core.exception.UnauthorizedException;
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
            return new SystemException("INVENTORY_REMOTE_CIRCUIT_OPEN",
                    "Inventory remote call rejected by circuit breaker: " + operation);
        }
        if (throwable.getClass().getSimpleName().contains("Bulkhead")) {
            return new SystemException("INVENTORY_REMOTE_BULKHEAD_FULL",
                    "Inventory remote call rejected by bulkhead: " + operation);
        }
        if (throwable instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            String message = "Inventory remote call failed: " + operation + ", status=" + statusCode;
            return switch (statusCode) {
                case 400 -> new BadRequestException("INVENTORY_REMOTE_BAD_REQUEST", message);
                case 401 -> new UnauthorizedException("INVENTORY_REMOTE_UNAUTHORIZED", message);
                case 403 -> new ForbiddenException("INVENTORY_REMOTE_FORBIDDEN", message);
                case 404 -> new NotFoundException("INVENTORY_REMOTE_NOT_FOUND", message);
                case 409 -> new ConflictException("INVENTORY_REMOTE_CONFLICT", message);
                default -> new SystemException("INVENTORY_REMOTE_ERROR", message);
            };
        }
        return new SystemException("INVENTORY_REMOTE_UNAVAILABLE",
                "Inventory remote service unavailable: " + operation);
    }
}
