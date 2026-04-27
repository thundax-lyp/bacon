package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum DepartmentErrorCode implements ErrorCode {
    DEPARTMENT_SORT_REQUIRED("UPMS-DEPT-400001", "Department sort must not be null", HttpStatus.BAD_REQUEST),
    INVALID_DEPARTMENT_SORT("UPMS-DEPT-400002", "Department sort must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NOT_FOUND("UPMS-DEPT-404001", "Department not found", HttpStatus.NOT_FOUND),
    PARENT_DEPARTMENT_NOT_FOUND("UPMS-DEPT-404002", "Parent department not found", HttpStatus.NOT_FOUND),
    DEPARTMENT_PARENT_CANNOT_BE_SELF("UPMS-DEPT-409001", "Department parent cannot be self", HttpStatus.CONFLICT),
    DEPARTMENT_HAS_CHILDREN("UPMS-DEPT-409002", "Department has child departments", HttpStatus.CONFLICT),
    DEPARTMENT_HAS_ASSIGNED_USERS("UPMS-DEPT-409003", "Department has assigned users", HttpStatus.CONFLICT);

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
