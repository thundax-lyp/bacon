package com.github.thundax.bacon.product.domain.model.enums;

public enum OutboxEventType {
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_STATUS_CHANGED,
    PRODUCT_ARCHIVED;

    public String value() {
        return name();
    }
}
