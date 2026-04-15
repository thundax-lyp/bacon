package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum MenuType {
    DIRECTORY,
    MENU,
    BUTTON,
    CATALOG;

    public String value() {
        return name();
    }

    public static MenuType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown menu type: " + value));
    }
}
