package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;

/**
 * 预占数量值对象。
 */
public record ReservedQuantity(int value) {

    public ReservedQuantity {
        if (value < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_QUANTITY, String.valueOf(value));
        }
    }

    public static ReservedQuantity of(Integer value) {
        if (value == null) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_QUANTITY, "null");
        }
        return new ReservedQuantity(value);
    }

    public boolean isEnough(int required) {
        return value >= required;
    }

    public boolean isZero() {
        return value == 0;
    }

    public ReservedQuantity increase(int delta) {
        if (delta < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_DELTA_QUANTITY, String.valueOf(delta));
        }
        return new ReservedQuantity(value + delta);
    }

    public ReservedQuantity decrease(int delta) {
        if (delta < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_DELTA_QUANTITY, String.valueOf(delta));
        }
        if (value < delta) {
            throw new InventoryDomainException(InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH, value + " < " + delta);
        }
        return new ReservedQuantity(value - delta);
    }
}
