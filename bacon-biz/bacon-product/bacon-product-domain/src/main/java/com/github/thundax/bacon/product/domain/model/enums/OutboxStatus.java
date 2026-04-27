package com.github.thundax.bacon.product.domain.model.enums;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    DEAD;

    public String value() {
        return name();
    }
}
