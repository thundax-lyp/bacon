package com.github.thundax.bacon.auth.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    INVALID_ACCESS_TOKEN("AUTH-400001", "Access token invalid", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("AUTH-400002", "Refresh token invalid", HttpStatus.BAD_REQUEST),
    INVALID_SESSION("AUTH-400003", "Session invalid", HttpStatus.BAD_REQUEST),
    INVALID_LOGIN_CREDENTIAL("AUTH-400004", "Invalid account or password", HttpStatus.BAD_REQUEST),
    ACCOUNT_DISABLED("AUTH-400005", "Current account is disabled", HttpStatus.BAD_REQUEST),
    USER_DISABLED("AUTH-400006", "Current user is not enabled", HttpStatus.BAD_REQUEST),
    CREDENTIAL_INACTIVE("AUTH-400007", "Current credential is not active", HttpStatus.BAD_REQUEST),
    CREDENTIAL_EXPIRED("AUTH-400008", "Current credential has expired", HttpStatus.BAD_REQUEST),
    TENANT_ID_REQUIRED("AUTH-400009", "tenantId must not be null", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_REQUIRED("AUTH-400010", "Old password required", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_INVALID("AUTH-400011", "New password invalid", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD("AUTH-400012", "New password must differ from old password", HttpStatus.BAD_REQUEST),
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
