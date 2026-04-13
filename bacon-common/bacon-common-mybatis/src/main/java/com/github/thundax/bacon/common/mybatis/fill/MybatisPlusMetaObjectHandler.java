package com.github.thundax.bacon.common.mybatis.fill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.mybatis.tenant.TenantScopeSupport;
import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import java.time.Clock;
import org.apache.ibatis.reflection.MetaObject;

public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private static final String TENANT_ID = "tenantId";

    public MybatisPlusMetaObjectHandler(Clock clock, CurrentUserProvider currentUserProvider) {}

    @Override
    public void insertFill(MetaObject metaObject) {
        fillTenantIdOnInsert(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        verifyTenantIdOnUpdate(metaObject);
    }

    private void fillTenantIdOnInsert(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        if (originalObject == null || !TenantScopeSupport.isInsertEnabled(originalObject.getClass())) {
            return;
        }
        Long currentTenantId = BaconContextHolder.currentTenantId();
        if (currentTenantId == null || !metaObject.hasGetter(TENANT_ID) || !metaObject.hasSetter(TENANT_ID)) {
            return;
        }
        if (getFieldValByName(TENANT_ID, metaObject) != null) {
            return;
        }
        Class<?> tenantIdType = metaObject.getGetterType(TENANT_ID);
        if (Long.class.equals(tenantIdType)) {
            setFieldValByName(TENANT_ID, currentTenantId, metaObject);
            return;
        }
        if (TenantId.class.equals(tenantIdType)) {
            setFieldValByName(TENANT_ID, TenantId.of(currentTenantId), metaObject);
        }
    }

    private void verifyTenantIdOnUpdate(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        if (originalObject == null || !TenantScopeSupport.isVerifyOnUpdateEnabled(originalObject.getClass())) {
            return;
        }
        Long currentTenantId = BaconContextHolder.currentTenantId();
        if (currentTenantId == null || !metaObject.hasGetter(TENANT_ID)) {
            return;
        }
        Object currentValue = getFieldValByName(TENANT_ID, metaObject);
        if (currentValue == null) {
            return;
        }
        Long entityTenantId = toTenantIdValue(currentValue);
        if (entityTenantId != null && !entityTenantId.equals(currentTenantId)) {
            throw new IllegalStateException(
                    "tenantId mismatch on update, context=" + currentTenantId + ", entity=" + entityTenantId);
        }
    }

    private Long toTenantIdValue(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof TenantId tenantId) {
            return tenantId.value();
        }
        return null;
    }
}
