package com.github.thundax.bacon.upms.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MenuErrorCode implements ErrorCode {
    MENU_REQUIRED_FIELD_BLANK("UPMS-MENU-400001", "Menu required field must not be blank", HttpStatus.BAD_REQUEST),
    MENU_SORT_REQUIRED("UPMS-MENU-400002", "Menu sort must not be null", HttpStatus.BAD_REQUEST),
    INVALID_MENU_SORT("UPMS-MENU-400003", "Menu sort must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    MENU_NOT_FOUND("UPMS-MENU-404001", "Menu not found", HttpStatus.NOT_FOUND),
    PARENT_MENU_NOT_FOUND("UPMS-MENU-404002", "Parent menu not found", HttpStatus.NOT_FOUND),
    MENU_PARENT_CANNOT_BE_SELF("UPMS-MENU-409001", "Menu parent cannot be self", HttpStatus.CONFLICT),
    MENU_HAS_CHILDREN("UPMS-MENU-409002", "Menu has child menus", HttpStatus.CONFLICT);

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
