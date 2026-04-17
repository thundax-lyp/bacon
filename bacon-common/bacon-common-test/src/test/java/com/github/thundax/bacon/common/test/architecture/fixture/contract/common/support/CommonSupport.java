package com.github.thundax.bacon.common.test.architecture.fixture.contract.common.support;

public final class CommonSupport {

    private CommonSupport() {}

    public static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
