package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TenantErrorCode implements ErrorCode {
    TENANT_INVALID_EXPIRED_AT("UPMS-TENANT-400001", "Tenant expiredAt must be in future", HttpStatus.BAD_REQUEST),
    TENANT_NOT_ACTIVE("UPMS-TENANT-409001", "Tenant is not active", HttpStatus.CONFLICT),
    TENANT_EXPIRED("UPMS-TENANT-409002", "Tenant is expired", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    TenantErrorCode(String code, String message, HttpStatus httpStatus) {
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
