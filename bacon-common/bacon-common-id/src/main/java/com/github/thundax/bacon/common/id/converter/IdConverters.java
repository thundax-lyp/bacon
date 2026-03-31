package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.core.Identifier;

import java.util.Objects;
import java.util.function.Function;

public final class IdConverters {

    private IdConverters() {
    }

    public static <T> T toValue(Identifier<T> identifier) {
        return identifier == null ? null : identifier.value();
    }

    public static <T, I> I fromValue(T value, Function<T, I> factory) {
        Objects.requireNonNull(factory, "factory must not be null");
        return value == null ? null : factory.apply(value);
    }
}
