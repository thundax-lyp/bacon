package com.github.thundax.bacon.order.domain.factory;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import java.time.Instant;

public class OrderFactory {

    public Order create(OrderId id, TenantId tenantId, OrderNo orderNo, UserId userId, CurrencyCode currencyCode,
                        Money totalAmount,
                        Money payableAmount, String remark, Instant expiredAt) {
        return new Order(id, tenantId, orderNo, userId, currencyCode, totalAmount, payableAmount, remark, expiredAt);
    }
}
