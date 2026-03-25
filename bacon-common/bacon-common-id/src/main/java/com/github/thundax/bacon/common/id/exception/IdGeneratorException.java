package com.github.thundax.bacon.common.id.exception;

import com.github.thundax.bacon.common.core.exception.BizException;

public class IdGeneratorException extends BizException {

    public IdGeneratorException(IdGeneratorErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public IdGeneratorException(IdGeneratorErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
