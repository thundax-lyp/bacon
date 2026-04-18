package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class MenuDomainException extends BizException {

    public MenuDomainException(MenuErrorCode errorCode) {
        super(errorCode);
    }

    public MenuDomainException(MenuErrorCode errorCode, String detail) {
        super(errorCode, errorCode.message() + ": " + detail);
    }
}
