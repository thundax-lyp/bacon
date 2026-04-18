package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.repository.OrderAuditLogRepository;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderAuditLogRepositoryImpl implements OrderAuditLogRepository {

    private final OrderAuditLogRepositorySupport support;

    public OrderAuditLogRepositoryImpl(OrderAuditLogRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insert(OrderAuditLog auditLog) {
        support.insert(auditLog);
    }

    @Override
    public List<OrderAuditLog> listByOrderNo(OrderNo orderNo) {
        return support.listByOrderNo(orderNo);
    }
}
