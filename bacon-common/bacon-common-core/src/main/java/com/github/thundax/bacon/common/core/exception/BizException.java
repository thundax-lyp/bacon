package com.github.thundax.bacon.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务异常基类，统一承载业务错误码、可读消息与 HTTP 状态。
 */
public class BizException extends BaconException {

    private final HttpStatus httpStatus;

    public BizException(ErrorCode errorCode) {
        this(errorCode, errorCode.message());
    }

    public BizException(ErrorCode errorCode, String message) {
        super(errorCode.code(), message);
        this.httpStatus = errorCode.httpStatus();
    }

    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.code(), message, cause);
        this.httpStatus = errorCode.httpStatus();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
