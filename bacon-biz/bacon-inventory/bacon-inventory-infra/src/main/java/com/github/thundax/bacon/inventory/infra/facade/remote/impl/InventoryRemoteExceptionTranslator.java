package com.github.thundax.bacon.inventory.infra.facade.remote.impl;

import com.github.thundax.bacon.common.core.exception.BaconException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.RestClientResponseException;

final class InventoryRemoteExceptionTranslator {

    private InventoryRemoteExceptionTranslator() {}

    static RuntimeException translate(String operation, Throwable throwable) {
        if (throwable instanceof BaconException baconException) {
            // 已经具备稳定业务语义的异常直接透传，避免 remote translator 覆盖原有错误码。
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
            // translator 只关心“库存远程调用失败”的分类，不负责推断 reserve/release/deduct 的业务结果。
            return switch (statusCode) {
                case 400 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_BAD_REQUEST, operation);
                case 401 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_UNAUTHORIZED, operation);
                case 403 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_FORBIDDEN, operation);
                case 404 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_NOT_FOUND, operation);
                case 409 -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_CONFLICT, operation);
                default ->
                    new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_REMOTE_ERROR, operation + ", status=" + statusCode);
            };
        }
        // 无法归类的异常统一落到远程不可用，保持上层重试和告警逻辑简单稳定。
        return new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_UNAVAILABLE, operation);
    }
}
