package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class UserDomainException extends BizException {

    public UserDomainException(UserErrorCode errorCode) {
        super(errorCode);
    }

    public UserDomainException(UserErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
