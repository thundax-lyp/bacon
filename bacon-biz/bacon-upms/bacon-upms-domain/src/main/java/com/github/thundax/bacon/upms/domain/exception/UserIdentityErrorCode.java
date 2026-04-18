package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserIdentityErrorCode implements ErrorCode {
    USER_IDENTITY_LOGIN_NOT_ALLOWED(
            "UPMS-IDENTITY-409002", "User identity login is not allowed", HttpStatus.CONFLICT),
    USER_IDENTITY_NOT_USABLE("UPMS-IDENTITY-409001", "User identity is not usable", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    UserIdentityErrorCode(String code, String message, HttpStatus httpStatus) {
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
