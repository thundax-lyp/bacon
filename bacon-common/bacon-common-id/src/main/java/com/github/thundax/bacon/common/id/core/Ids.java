package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.MenuId;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.PostId;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;

public interface Ids {

    DepartmentId departmentId();

    MenuId menuId();

    PostId postId();

    ResourceId resourceId();

    UserCredentialId userCredentialId();

    UserId userId();

    UserIdentityId userIdentityId();

    RoleId roleId();

    OrderId orderId();

    StoredObjectId storedObjectId();
}
