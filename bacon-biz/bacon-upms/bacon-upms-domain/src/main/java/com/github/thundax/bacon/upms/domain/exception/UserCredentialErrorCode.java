package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserCredentialErrorCode implements ErrorCode {
    USER_CREDENTIAL_PASSWORD_CHANGE_NOT_SUPPORTED(
            "UPMS-CREDENTIAL-400001",
            "User credential password change requirement is only supported for password credentials",
            HttpStatus.BAD_REQUEST),
    USER_CREDENTIAL_NOT_ACTIVE(
            "UPMS-CREDENTIAL-409001", "User credential is not active", HttpStatus.CONFLICT),
    USER_CREDENTIAL_LOCKED(
            "UPMS-CREDENTIAL-409002", "User credential is locked", HttpStatus.CONFLICT),
    USER_CREDENTIAL_EXPIRED(
            "UPMS-CREDENTIAL-409003", "User credential is expired", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    UserCredentialErrorCode(String code, String message, HttpStatus httpStatus) {
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
