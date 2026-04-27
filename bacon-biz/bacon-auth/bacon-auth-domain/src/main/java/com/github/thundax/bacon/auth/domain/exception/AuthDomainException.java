package com.github.thundax.bacon.auth.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class AuthDomainException extends BizException {

    public AuthDomainException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    public AuthDomainException(AuthErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }

    public AuthDomainException(AuthErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, errorCode.message() + ": " + detail, cause);
    }
}
