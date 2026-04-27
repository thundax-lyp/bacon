package com.github.thundax.bacon.product.domain.model.enums;

public enum ImageType {
    MAIN,
    GALLERY,
    DETAIL;

    public String value() {
        return name();
    }
}
