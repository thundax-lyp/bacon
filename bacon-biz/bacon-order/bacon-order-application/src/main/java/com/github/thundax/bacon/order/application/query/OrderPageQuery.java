package com.github.thundax.bacon.order.application.query;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import java.time.Instant;

public record OrderPageQuery(
        UserId userId,
        OrderNo orderNo,
        OrderStatus orderStatus,
        PayStatus payStatus,
        InventoryStatus inventoryStatus,
        Instant createdAtFrom,
        Instant createdAtTo,
        Integer pageNo,
        Integer pageSize) {}
