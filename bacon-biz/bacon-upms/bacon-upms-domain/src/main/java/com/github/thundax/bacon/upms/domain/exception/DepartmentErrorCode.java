package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum DepartmentErrorCode implements ErrorCode {
    DEPARTMENT_PARENT_CANNOT_BE_SELF("UPMS-DEPT-409001", "Department parent cannot be self", HttpStatus.CONFLICT),
    INVALID_DEPARTMENT_SORT("UPMS-DEPT-400001", "Department sort must be greater than or equal to 0", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    DepartmentErrorCode(String code, String message, HttpStatus httpStatus) {
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
