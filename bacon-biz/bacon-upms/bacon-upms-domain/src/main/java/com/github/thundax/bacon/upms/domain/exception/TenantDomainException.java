package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class TenantDomainException extends BizException {

    public TenantDomainException(TenantErrorCode errorCode) {
        super(errorCode);
    }

    public TenantDomainException(TenantErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
