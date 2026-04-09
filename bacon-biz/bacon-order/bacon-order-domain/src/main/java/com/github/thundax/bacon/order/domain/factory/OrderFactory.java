package com.github.thundax.bacon.order.domain.factory;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import java.time.Instant;

public class OrderFactory {

    public Order create(OrderId id, TenantId tenantId, OrderNo orderNo, UserId userId, CurrencyCode currencyCode,
                        Money totalAmount,
                        Money payableAmount, String remark, Instant expiredAt) {
        return new Order(id == null ? null : id.value(),
                tenantId == null ? null : tenantId.value(),
                orderNo == null ? null : orderNo.value(),
                userId == null ? null : Long.valueOf(userId.value()),
                currencyCode,
                totalAmount == null ? null : totalAmount.value().toPlainString(),
                payableAmount == null ? null : payableAmount.value().toPlainString(),
                remark,
                expiredAt);
    }
}
