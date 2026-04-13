package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;

public final class MenuIdCodec {

    private MenuIdCodec() {}

    public static MenuId toDomain(Long value) {
        return value == null ? null : MenuId.of(value);
    }

    public static Long toValue(MenuId value) {
        return value == null ? null : value.value();
    }
}
