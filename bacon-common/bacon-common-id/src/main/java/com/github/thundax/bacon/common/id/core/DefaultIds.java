package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.UserId;

public class DefaultIds implements Ids {

    private static final String DEPARTMENT_ID_BIZ_TAG = "department-id";
    private static final String USER_ID_BIZ_TAG = "user-id";
    private static final String ROLE_ID_BIZ_TAG = "role-id";
    private static final String ORDER_ID_BIZ_TAG = "order-id";
    private static final String SKU_ID_BIZ_TAG = "sku-id";

    private final IdGenerator idGenerator;

    public DefaultIds(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public DepartmentId departmentId() {
        return DepartmentId.of("D" + idGenerator.nextId(DEPARTMENT_ID_BIZ_TAG));
    }

    @Override
    public UserId userId() {
        return UserId.of("U" + idGenerator.nextId(USER_ID_BIZ_TAG));
    }

    @Override
    public RoleId roleId() {
        return RoleId.of(idGenerator.nextId(ROLE_ID_BIZ_TAG));
    }

    @Override
    public OrderId orderId() {
        return OrderId.of(idGenerator.nextId(ORDER_ID_BIZ_TAG));
    }

    @Override
    public SkuId skuId() {
        return SkuId.of(idGenerator.nextId(SKU_ID_BIZ_TAG));
    }
}
