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
        String currentUserId = currentUserId();
        fillIfPresent(metaObject, CREATED_BY, String.class, currentUserId, true);
        fillIfPresent(metaObject, CREATED_AT, LocalDateTime.class, now, true);
        fillIfPresent(metaObject, UPDATED_BY, String.class, currentUserId, true);
        fillIfPresent(metaObject, UPDATED_AT, LocalDateTime.class, now, true);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(clock);
        String currentUserId = currentUserId();
        fillIfPresent(metaObject, UPDATED_BY, String.class, currentUserId, false);
        fillIfPresent(metaObject, UPDATED_AT, LocalDateTime.class, now, false);
    }

    private String currentUserId() {
        String currentUserId = currentUserProvider.currentUserId();
        return currentUserId == null || currentUserId.isBlank() ? "system" : currentUserId;
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
