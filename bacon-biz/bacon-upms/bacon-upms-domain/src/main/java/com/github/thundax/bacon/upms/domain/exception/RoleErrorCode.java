package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RoleErrorCode implements ErrorCode {
    INVALID_ROLE_NAME("UPMS-ROLE-400001", "Role name must not be blank", HttpStatus.BAD_REQUEST),
    ROLE_NOT_ACTIVE("UPMS-ROLE-409001", "Role is not active", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    RoleErrorCode(String code, String message, HttpStatus httpStatus) {
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
