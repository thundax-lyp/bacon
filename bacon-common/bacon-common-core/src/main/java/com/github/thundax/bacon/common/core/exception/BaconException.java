package com.github.thundax.bacon.common.core.exception;

/**
 * 通用异常基类，用于统一承载错误码和异常消息。
 */
public class BaconException extends RuntimeException {

    private final String code;

    public BaconException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BaconException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
