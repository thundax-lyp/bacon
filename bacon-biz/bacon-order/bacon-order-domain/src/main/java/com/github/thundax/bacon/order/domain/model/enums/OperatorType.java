package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 操作人类型。
 */
public enum OperatorType {

    SYSTEM("SYSTEM"),
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    OperatorType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OperatorType fromValue(String value) {
        for (OperatorType operatorType : values()) {
            if (operatorType.value.equals(value)) {
                return operatorType;
            }
        }
        throw new IllegalArgumentException("Unsupported operator type: " + value);
    }
}
