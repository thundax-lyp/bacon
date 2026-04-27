package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TenantErrorCode implements ErrorCode {
    TENANT_REQUIRED_FIELD_BLANK(
            "UPMS-TENANT-400001", "Tenant required field must not be blank", HttpStatus.BAD_REQUEST),
    TENANT_CODE_REQUIRED("UPMS-TENANT-400002", "Tenant code must not be null", HttpStatus.BAD_REQUEST),
    TENANT_STATUS_REQUIRED("UPMS-TENANT-400003", "Tenant status must not be null", HttpStatus.BAD_REQUEST),
    TENANT_INVALID_EXPIRED_AT("UPMS-TENANT-400004", "Tenant expiredAt must be in future", HttpStatus.BAD_REQUEST),
    TENANT_NOT_FOUND("UPMS-TENANT-404001", "Tenant not found", HttpStatus.NOT_FOUND),
    TENANT_NOT_ACTIVE("UPMS-TENANT-409001", "Tenant is not active", HttpStatus.CONFLICT),
    TENANT_EXPIRED("UPMS-TENANT-409002", "Tenant is expired", HttpStatus.CONFLICT),
    TENANT_CODE_ALREADY_EXISTS("UPMS-TENANT-409003", "Tenant code already exists", HttpStatus.CONFLICT);

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
