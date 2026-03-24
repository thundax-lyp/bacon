package com.github.thundax.bacon.common.core.exception;

/**
 * 表示资源状态冲突或重复操作。
 */
public class ConflictException extends BaconException {

    private static final String DEFAULT_CODE = "CONFLICT";

    public ConflictException(String message) {
        super(DEFAULT_CODE, message);
    }

    public ConflictException(String code, String message) {
        super(code, message);
    }

    public ConflictException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
