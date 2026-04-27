package com.github.thundax.bacon.product.domain.model.enums;

public enum IdempotencyStatus {
    PROCESSING,
    SUCCESS,
    FAILED;

    public String value() {
        return name();
    }
}
