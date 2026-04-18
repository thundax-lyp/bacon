package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MenuErrorCode implements ErrorCode {
    INVALID_MENU_SORT("UPMS-MENU-400001", "Menu sort must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    MENU_PARENT_CANNOT_BE_SELF("UPMS-MENU-409001", "Menu parent cannot be self", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    MenuErrorCode(String code, String message, HttpStatus httpStatus) {
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
