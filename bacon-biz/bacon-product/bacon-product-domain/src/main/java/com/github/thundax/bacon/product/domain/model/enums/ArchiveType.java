package com.github.thundax.bacon.product.domain.model.enums;

public enum ArchiveType {
    CREATE,
    UPDATE_BASE,
    UPDATE_SKU,
    UPDATE_IMAGE,
    STATUS_CHANGE,
    ARCHIVE;

    public String value() {
        return name();
    }
}
