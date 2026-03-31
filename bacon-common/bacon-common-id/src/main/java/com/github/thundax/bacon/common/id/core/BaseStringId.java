package com.github.thundax.bacon.common.id.core;

public abstract class BaseStringId extends BaseId<String> {

    protected BaseStringId(String value) {
        super(value, String.class);
    }

    @Override
    protected void validate(String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
    }
}
