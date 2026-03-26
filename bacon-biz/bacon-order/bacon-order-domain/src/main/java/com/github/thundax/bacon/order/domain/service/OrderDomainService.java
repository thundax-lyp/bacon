package com.github.thundax.bacon.order.domain.service;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import java.math.BigDecimal;
import java.time.Instant;

public class OrderDomainService {

    public Order create(Long id, Long tenantId, String orderNo, Long userId, String currencyCode,
                        BigDecimal totalAmount, BigDecimal payableAmount, String remark, Instant expiredAt) {
        return new Order(id, tenantId, orderNo, userId, currencyCode, totalAmount, payableAmount, remark, expiredAt);
    }
}
