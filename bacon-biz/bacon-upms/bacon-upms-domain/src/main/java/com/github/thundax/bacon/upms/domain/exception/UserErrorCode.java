package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("UPMS-USER-404001", "User not found", HttpStatus.NOT_FOUND),
    USER_NOT_ACTIVE("UPMS-USER-409001", "User is not active", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    UserErrorCode(String code, String message, HttpStatus httpStatus) {
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
