package com.github.thundax.bacon.order.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {
    INVALID_ORDER_STATUS("ORD-400001", "Invalid order status", HttpStatus.BAD_REQUEST),
    INVALID_IDEMPOTENCY_STATUS("ORD-400002", "Invalid idempotency status", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_ITEM("ORD-400003", "Invalid order item", HttpStatus.BAD_REQUEST),
    INVALID_OUTBOX_DEAD_LETTER("ORD-400004", "Invalid outbox dead letter", HttpStatus.BAD_REQUEST),
    INVALID_OUTBOX_EVENT("ORD-400005", "Invalid outbox event", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    OrderErrorCode(String code, String message, HttpStatus httpStatus) {
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
