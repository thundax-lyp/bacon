package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class UserIdentityDomainException extends BizException {

    public UserIdentityDomainException(UserIdentityErrorCode errorCode) {
        super(errorCode);
    }

    public UserIdentityDomainException(UserIdentityErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
