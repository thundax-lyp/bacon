package com.github.thundax.bacon.common.id.mapper;

import com.github.thundax.bacon.common.id.domain.UserId;

public final class UserIdMapper {

    private UserIdMapper() {
    }

    public static UserId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return UserId.of(value);
    }
}
