package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum ImageType {
    MAIN,
    GALLERY,
    DETAIL;

    public String value() {
        return name();
    }

    public static ImageType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown image type: " + value));
    }
}
