package com.github.thundax.bacon.common.core.exception;

/**
 * 表示请求方无权执行当前操作。
 */
public class ForbiddenException extends BaconException {

    private static final String DEFAULT_CODE = "FORBIDDEN";

    public ForbiddenException(String message) {
        super(DEFAULT_CODE, message);
    }

    public ForbiddenException(String code, String message) {
        super(code, message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
