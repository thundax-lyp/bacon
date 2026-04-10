package com.github.thundax.bacon.common.core.valueobject;

/**
 * 乐观锁版本号。
 */
public record Version(long value) {

    public Version {
        if (value < 0) {
            throw new IllegalArgumentException("version must >= 0");
        }
    }

    public Version next() {
        return new Version(this.value + 1);
    }
}
