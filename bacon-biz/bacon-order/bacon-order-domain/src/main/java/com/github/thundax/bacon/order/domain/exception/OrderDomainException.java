package com.github.thundax.bacon.order.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class OrderDomainException extends BizException {

    public OrderDomainException(OrderErrorCode errorCode) {
        super(errorCode);
    }

    public OrderDomainException(OrderErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }

    public OrderDomainException(OrderErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, errorCode.message() + ": " + detail, cause);
    }
}
