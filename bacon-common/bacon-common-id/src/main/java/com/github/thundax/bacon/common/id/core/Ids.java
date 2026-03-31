package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.UserId;

public interface Ids {

    UserId userId();

    RoleId roleId();

    OrderId orderId();

    SkuId skuId();
}
