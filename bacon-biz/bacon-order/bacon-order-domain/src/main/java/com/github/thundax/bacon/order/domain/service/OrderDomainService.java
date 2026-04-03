package com.github.thundax.bacon.order.domain.service;

import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import java.time.Instant;

public class OrderDomainService {

    public Order create(OrderId id, Long tenantId, String orderNo, Long userId, Money totalAmount,
                        Money payableAmount, String remark, Instant expiredAt) {
        return new Order(id, tenantId, orderNo, userId, totalAmount, payableAmount, remark, expiredAt);
    }
}
