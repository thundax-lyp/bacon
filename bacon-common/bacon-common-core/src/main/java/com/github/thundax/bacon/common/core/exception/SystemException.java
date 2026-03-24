package com.github.thundax.bacon.common.core.exception;

/**
 * 表示系统内部异常或不可恢复的运行时错误。
 */
public class SystemException extends BaconException {

    private static final String DEFAULT_CODE = "SYSTEM_ERROR";

    public SystemException(String message) {
        super(DEFAULT_CODE, message);
    }

    public SystemException(String code, String message) {
        super(code, message);
    }

    public SystemException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
