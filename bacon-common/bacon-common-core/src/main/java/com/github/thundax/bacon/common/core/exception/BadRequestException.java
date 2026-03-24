package com.github.thundax.bacon.common.core.exception;

/**
 * 表示请求参数或请求状态不合法。
 */
public class BadRequestException extends BaconException {

    private static final String DEFAULT_CODE = "BAD_REQUEST";

    public BadRequestException(String message) {
        super(DEFAULT_CODE, message);
    }

    public BadRequestException(String code, String message) {
        super(code, message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
