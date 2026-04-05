package com.github.thundax.bacon.common.mybatis.fill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Clock;
import java.time.LocalDateTime;

public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATED_BY = "createdBy";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_BY = "updatedBy";
    private static final String UPDATED_AT = "updatedAt";

    private final Clock clock;
    private final CurrentUserProvider currentUserProvider;

    public MybatisPlusMetaObjectHandler(Clock clock, CurrentUserProvider currentUserProvider) {
        this.clock = clock;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(clock);
        fillAuditUser(metaObject, CREATED_BY, true);
        fillIfPresent(metaObject, CREATED_AT, LocalDateTime.class, now, true);
        fillAuditUser(metaObject, UPDATED_BY, true);
        fillIfPresent(metaObject, UPDATED_AT, LocalDateTime.class, now, true);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(clock);
        fillAuditUser(metaObject, UPDATED_BY, false);
        fillIfPresent(metaObject, UPDATED_AT, LocalDateTime.class, now, false);
    }

    private String currentUserId() {
        String currentUserId = currentUserProvider.currentUserId();
        return currentUserId == null || currentUserId.isBlank() ? "system" : currentUserId;
    }

    private Long currentUserIdAsLong() {
        String currentUserId = currentUserProvider.currentUserId();
        if (currentUserId == null || currentUserId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(currentUserId);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private void fillAuditUser(MetaObject metaObject, String fieldName, boolean insert) {
        if (!metaObject.hasGetter(fieldName) || !metaObject.hasSetter(fieldName)) {
            return;
        }
        Class<?> setterType = metaObject.getSetterType(fieldName);
        if (Long.class.equals(setterType)) {
            fillIfPresent(metaObject, fieldName, Long.class, currentUserIdAsLong(), insert);
            return;
        }
        if (String.class.equals(setterType)) {
            fillIfPresent(metaObject, fieldName, String.class, currentUserId(), insert);
        }
    }

    private <T> void fillIfPresent(MetaObject metaObject, String fieldName, Class<T> fieldType,
                                   T fieldValue, boolean insert) {
        if (!metaObject.hasGetter(fieldName) || !metaObject.hasSetter(fieldName)) {
            return;
        }
        if (insert) {
            strictInsertFill(metaObject, fieldName, fieldType, fieldValue);
            return;
        }
        strictUpdateFill(metaObject, fieldName, fieldType, fieldValue);
    }
}
