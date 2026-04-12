package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;

/**
 * 在库数量值对象。
 */
public record OnHandQuantity(int value) {

    public OnHandQuantity {
        if (value < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_ON_HAND_QUANTITY, String.valueOf(value));
        }
    }

    public static OnHandQuantity of(Integer value) {
        if (value == null) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_ON_HAND_QUANTITY, "null");
        }
        return new OnHandQuantity(value);
    }

    public boolean isEnough(int required) {
        return value >= required;
    }

    public boolean isZero() {
        return value == 0;
    }

    public OnHandQuantity increase(int delta) {
        if (delta < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_DELTA_QUANTITY, String.valueOf(delta));
        }
        return new OnHandQuantity(value + delta);
    }

    public OnHandQuantity decrease(int delta) {
        if (delta < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_DELTA_QUANTITY, String.valueOf(delta));
        }
        if (this.value < delta) {
            throw new InventoryDomainException(InventoryErrorCode.INSUFFICIENT_STOCK, this.value + " < " + delta);
        }
        return new OnHandQuantity(value - delta);
    }
}
