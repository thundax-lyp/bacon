package com.github.thundax.bacon.common.id.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum IdGeneratorErrorCode implements ErrorCode {
    ID_PROVIDER_UNAVAILABLE("ID-1001", "发号服务不可用", HttpStatus.SERVICE_UNAVAILABLE),
    ID_PROVIDER_RESPONSE_INVALID("ID-1002", "发号服务响应不合法", HttpStatus.BAD_GATEWAY),
    ID_PROVIDER_NOT_SUPPORTED("ID-1003", "不支持的发号Provider", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    IdGeneratorErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
