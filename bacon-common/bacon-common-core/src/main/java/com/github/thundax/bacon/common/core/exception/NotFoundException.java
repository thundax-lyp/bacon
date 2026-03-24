package com.github.thundax.bacon.common.core.exception;

/**
 * 表示目标资源不存在。
 */
public class NotFoundException extends BaconException {

    private static final String DEFAULT_CODE = "NOT_FOUND";

    public NotFoundException(String message) {
        super(DEFAULT_CODE, message);
    }

    public NotFoundException(String code, String message) {
        super(code, message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
