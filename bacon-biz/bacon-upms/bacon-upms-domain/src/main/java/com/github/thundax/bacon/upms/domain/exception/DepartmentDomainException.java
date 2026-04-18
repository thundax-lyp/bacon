package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class DepartmentDomainException extends BizException {

    public DepartmentDomainException(DepartmentErrorCode errorCode) {
        super(errorCode);
    }

    public DepartmentDomainException(DepartmentErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
