package com.github.thundax.bacon.common.id.core;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class BaseId<T> implements Identifier<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final T value;
    private final Class<T> type;

    protected BaseId(T value, Class<T> type) {
        this.value = Objects.requireNonNull(value, "id value must not be null");
        this.type = Objects.requireNonNull(type, "id type must not be null");
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("id value type mismatch: expected " + type.getName()
                    + " but was " + value.getClass().getName());
        }
        validate(value);
    }

    protected void validate(T value) {
    }

    @Override
    @JsonValue
    public final T value() {
        return value;
    }

    @Override
    public final Class<T> type() {
        return type;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BaseId<?> baseId = (BaseId<?>) other;
        return value.equals(baseId.value);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public String toString() {
        return asString();
    }
}
