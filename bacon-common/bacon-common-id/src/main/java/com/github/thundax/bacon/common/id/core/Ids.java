package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.MenuId;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.PostId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.UserId;

public interface Ids {

    DepartmentId departmentId();

    MenuId menuId();

    PostId postId();

    UserId userId();

    RoleId roleId();

    OrderId orderId();

    SkuId skuId();
}
