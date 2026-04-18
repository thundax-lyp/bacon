package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.BizException;
import com.github.thundax.bacon.common.core.exception.ErrorCode;

public class UpmsDomainException extends BizException {

    public UpmsDomainException(ErrorCode errorCode) {
        super(errorCode);
    }
}
