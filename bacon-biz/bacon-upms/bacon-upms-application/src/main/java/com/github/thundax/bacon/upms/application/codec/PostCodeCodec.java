package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.PostCode;

public final class PostCodeCodec {

    private PostCodeCodec() {}

    public static PostCode toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return PostCode.of(value.trim());
    }

    public static String toValue(PostCode value) {
        return value == null ? null : value.value();
    }
}
