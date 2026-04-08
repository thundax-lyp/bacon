package com.github.thundax.bacon.inventory.domain.model.valueobject;

/**
 * 回放任务业务编号。
 */
public record TaskNo(String value) {

    public TaskNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("taskNo must not be blank");
        }
    }

    public static TaskNo of(String value) {
        return new TaskNo(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
