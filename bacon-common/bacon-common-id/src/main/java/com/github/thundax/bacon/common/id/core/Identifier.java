package com.github.thundax.bacon.common.id.core;

public interface Identifier<T> {

    T value();

    Class<T> type();

    default String asString() {
        return String.valueOf(value());
    }
}
