package com.github.thundax.bacon.auth.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    INVALID_ACCESS_TOKEN("AUTH-400001", "Access token invalid", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("AUTH-400002", "Refresh token invalid", HttpStatus.BAD_REQUEST),
    INVALID_SESSION("AUTH-400003", "Session invalid", HttpStatus.BAD_REQUEST),
    SESSION_NOT_FOUND("AUTH-404001", "Session not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(String code, String message, HttpStatus httpStatus) {
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
