package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;

/**
 * 可用数量值对象。
 */
public record AvailableQuantity(int value) {

    public AvailableQuantity {
        if (value < 0) {
            throw new InventoryDomainException(
                    InventoryErrorCode.INSUFFICIENT_AVAILABLE_STOCK,
                    String.valueOf(value));
        }
    }

    public boolean isEnough(int required) {
        if (required < 0) {
            throw new InventoryDomainException(
                    InventoryErrorCode.INVALID_REQUIRED_QUANTITY,
                    String.valueOf(required));
        }
        return this.value >= required;
    }
}
