package com.github.thundax.bacon.common.id.core;

public abstract class BaseLongId extends BaseId<Long> {

    protected BaseLongId(Long value) {
        super(value, Long.class);
    }

    @Override
    protected void validate(Long value) {
        if (value <= 0L) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
