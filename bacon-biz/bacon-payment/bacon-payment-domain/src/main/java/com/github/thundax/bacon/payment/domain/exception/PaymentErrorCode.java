package com.github.thundax.bacon.payment.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_NOT_FOUND("PAY-404001", "Payment not found", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_AMOUNT("PAY-400001", "Invalid payment amount", HttpStatus.BAD_REQUEST),
    INVALID_CHANNEL_CODE("PAY-400002", "Invalid channel code", HttpStatus.BAD_REQUEST),
    INVALID_EXPIRED_AT("PAY-400003", "Invalid expired at", HttpStatus.BAD_REQUEST),
    INVALID_CLOSE_REASON("PAY-400004", "Invalid close reason", HttpStatus.BAD_REQUEST),
    INVALID_CALLBACK_REQUEST("PAY-400005", "Invalid callback request", HttpStatus.BAD_REQUEST),
    PAYMENT_PERSISTENCE_CONFLICT("PAY-409001", "Payment persistence conflict", HttpStatus.CONFLICT),
    PAYMENT_REMOTE_BAD_REQUEST("PAY-400901", "Payment remote bad request", HttpStatus.BAD_REQUEST),
    PAYMENT_REMOTE_UNAUTHORIZED("PAY-401901", "Payment remote unauthorized", HttpStatus.UNAUTHORIZED),
    PAYMENT_REMOTE_FORBIDDEN("PAY-403901", "Payment remote forbidden", HttpStatus.FORBIDDEN),
    PAYMENT_REMOTE_NOT_FOUND("PAY-404901", "Payment remote not found", HttpStatus.NOT_FOUND),
    PAYMENT_REMOTE_CONFLICT("PAY-409901", "Payment remote conflict", HttpStatus.CONFLICT),
    PAYMENT_REMOTE_CIRCUIT_OPEN("PAY-503901", "Payment remote circuit open", HttpStatus.SERVICE_UNAVAILABLE),
    PAYMENT_REMOTE_BULKHEAD_FULL("PAY-503902", "Payment remote bulkhead full", HttpStatus.SERVICE_UNAVAILABLE),
    PAYMENT_REMOTE_UNAVAILABLE("PAY-503903", "Payment remote unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    PAYMENT_REMOTE_ERROR("PAY-500901", "Payment remote internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    PaymentErrorCode(String code, String message, HttpStatus httpStatus) {
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
