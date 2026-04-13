package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;

public final class PostIdCodec {

    private PostIdCodec() {}

    public static PostId toDomain(Long value) {
        return value == null ? null : PostId.of(value);
    }

    public static Long toValue(PostId value) {
        return value == null ? null : value.value();
    }
}
