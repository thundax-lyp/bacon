package com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums;

public enum EnumFieldFixture {

    ENABLED("ENABLED"),
    DISABLED("DISABLED");

    private final String value;

    EnumFieldFixture(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
