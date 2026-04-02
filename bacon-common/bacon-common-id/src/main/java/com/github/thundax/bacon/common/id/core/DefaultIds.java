package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.MenuId;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.PostId;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;

public class DefaultIds implements Ids {

    private static final String DEPARTMENT_ID_BIZ_TAG = "department-id";
    private static final String MENU_ID_BIZ_TAG = "menu-id";
    private static final String POST_ID_BIZ_TAG = "post-id";
    private static final String RESOURCE_ID_BIZ_TAG = "resource-id";
    private static final String USER_CREDENTIAL_ID_BIZ_TAG = "user-credential-id";
    private static final String USER_ID_BIZ_TAG = "user-id";
    private static final String USER_IDENTITY_ID_BIZ_TAG = "user-identity-id";
    private static final String ROLE_ID_BIZ_TAG = "role-id";
    private static final String ORDER_ID_BIZ_TAG = "order-id";
    private static final String SKU_ID_BIZ_TAG = "sku-id";
    private static final String STORED_OBJECT_ID_BIZ_TAG = "stored-object-id";

    private final IdGenerator idGenerator;

    public DefaultIds(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public DepartmentId departmentId() {
        return DepartmentId.of("D" + idGenerator.nextId(DEPARTMENT_ID_BIZ_TAG));
    }

    @Override
    public MenuId menuId() {
        return MenuId.of("M" + idGenerator.nextId(MENU_ID_BIZ_TAG));
    }

    @Override
    public PostId postId() {
        return PostId.of("P" + idGenerator.nextId(POST_ID_BIZ_TAG));
    }

    @Override
    public ResourceId resourceId() {
        return ResourceId.of("R" + idGenerator.nextId(RESOURCE_ID_BIZ_TAG));
    }

    @Override
    public UserCredentialId userCredentialId() {
        return UserCredentialId.of("C" + idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG));
    }

    @Override
    public UserId userId() {
        return UserId.of("U" + idGenerator.nextId(USER_ID_BIZ_TAG));
    }

    @Override
    public UserIdentityId userIdentityId() {
        return UserIdentityId.of("I" + idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG));
    }

    @Override
    public RoleId roleId() {
        return RoleId.of(String.valueOf(idGenerator.nextId(ROLE_ID_BIZ_TAG)));
    }

    @Override
    public OrderId orderId() {
        return OrderId.of(idGenerator.nextId(ORDER_ID_BIZ_TAG));
    }

    @Override
    public SkuId skuId() {
        return SkuId.of(idGenerator.nextId(SKU_ID_BIZ_TAG));
    }

    @Override
    public StoredObjectId storedObjectId() {
        return StoredObjectId.of("O" + idGenerator.nextId(STORED_OBJECT_ID_BIZ_TAG));
    }
}
