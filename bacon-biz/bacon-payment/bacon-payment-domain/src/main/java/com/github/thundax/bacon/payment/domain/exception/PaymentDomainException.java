package com.github.thundax.bacon.payment.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class PaymentDomainException extends BizException {

    public PaymentDomainException(PaymentErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentDomainException(PaymentErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }

    public PaymentDomainException(PaymentErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, errorCode.message() + ": " + detail, cause);
    }
}
