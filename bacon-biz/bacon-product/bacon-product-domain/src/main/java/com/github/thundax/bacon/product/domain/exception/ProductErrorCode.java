package com.github.thundax.bacon.product.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ProductErrorCode implements ErrorCode {
    INVALID_PRODUCT("PRD-400001", "Invalid product", HttpStatus.BAD_REQUEST),
    INVALID_SKU("PRD-400002", "Invalid sku", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY("PRD-400003", "Invalid category", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE("PRD-400004", "Invalid image", HttpStatus.BAD_REQUEST),
    INVALID_SNAPSHOT("PRD-400005", "Invalid product snapshot", HttpStatus.BAD_REQUEST),
    INVALID_ARCHIVE("PRD-400006", "Invalid product archive", HttpStatus.BAD_REQUEST),
    INVALID_IDEMPOTENCY_RECORD("PRD-400007", "Invalid product idempotency record", HttpStatus.BAD_REQUEST),
    INVALID_OUTBOX("PRD-400008", "Invalid product outbox", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_STATUS("PRD-409001", "Invalid product status", HttpStatus.CONFLICT),
    INVALID_SKU_STATUS("PRD-409002", "Invalid sku status", HttpStatus.CONFLICT),
    VERSION_CONFLICT("PRD-409003", "Product version conflict", HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_CONFLICT("PRD-409004", "Product idempotency key conflict", HttpStatus.CONFLICT),
    PRODUCT_NOT_FOUND("PRD-404001", "Product not found", HttpStatus.NOT_FOUND),
    SKU_NOT_FOUND("PRD-404002", "Sku not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("PRD-404003", "Category not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ProductErrorCode(String code, String message, HttpStatus httpStatus) {
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
