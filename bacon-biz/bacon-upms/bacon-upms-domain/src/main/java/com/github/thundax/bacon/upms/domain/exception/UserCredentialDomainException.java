package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class UserCredentialDomainException extends BizException {

    public UserCredentialDomainException(UserCredentialErrorCode errorCode) {
        super(errorCode);
    }

    public UserCredentialDomainException(UserCredentialErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
