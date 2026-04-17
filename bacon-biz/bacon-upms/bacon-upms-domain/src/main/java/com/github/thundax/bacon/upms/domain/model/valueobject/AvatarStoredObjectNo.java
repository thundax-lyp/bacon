package com.github.thundax.bacon.upms.domain.model.valueobject;

/**
 * 用户头像存储对象编号。
 */
public record AvatarStoredObjectNo(String value) {

    public AvatarStoredObjectNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("avatarStoredObjectNo must not be blank");
        }
    }

    public static AvatarStoredObjectNo of(String value) {
        return new AvatarStoredObjectNo(value.trim());
    }
}
