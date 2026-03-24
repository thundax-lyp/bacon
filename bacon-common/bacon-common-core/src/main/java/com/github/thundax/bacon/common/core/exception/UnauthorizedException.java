package com.github.thundax.bacon.common.core.exception;

/**
 * 表示请求方未完成认证或认证信息无效。
 */
public class UnauthorizedException extends BaconException {

    private static final String DEFAULT_CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(DEFAULT_CODE, message);
    }

    public UnauthorizedException(String code, String message) {
        super(code, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
