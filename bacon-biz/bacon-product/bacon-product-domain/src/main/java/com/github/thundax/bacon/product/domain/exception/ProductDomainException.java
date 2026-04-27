package com.github.thundax.bacon.product.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class ProductDomainException extends BizException {

    public ProductDomainException(ProductErrorCode errorCode) {
        super(errorCode);
    }

    public ProductDomainException(ProductErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
