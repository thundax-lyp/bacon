package com.github.thundax.bacon.inventory.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class InventoryDomainException extends BizException {

    public InventoryDomainException(InventoryErrorCode errorCode) {
        super(errorCode);
    }

    public InventoryDomainException(InventoryErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }

    public InventoryDomainException(InventoryErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, errorCode.message() + ": " + detail, cause);
    }
}
