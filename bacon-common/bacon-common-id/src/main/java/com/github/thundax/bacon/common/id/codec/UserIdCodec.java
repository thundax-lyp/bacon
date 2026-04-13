package com.github.thundax.bacon.common.id.codec;

import com.github.thundax.bacon.common.id.domain.UserId;

public final class UserIdCodec {

    private UserIdCodec() {}

    public static UserId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return UserId.of(value);
    }

    public static Long toValue(UserId userId) {
        return userId == null ? null : userId.value();
    }
}
