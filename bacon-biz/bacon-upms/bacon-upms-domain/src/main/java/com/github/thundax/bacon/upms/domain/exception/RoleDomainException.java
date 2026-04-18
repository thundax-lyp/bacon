package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class RoleDomainException extends BizException {

    public RoleDomainException(RoleErrorCode errorCode) {
        super(errorCode);
    }

    public RoleDomainException(RoleErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
