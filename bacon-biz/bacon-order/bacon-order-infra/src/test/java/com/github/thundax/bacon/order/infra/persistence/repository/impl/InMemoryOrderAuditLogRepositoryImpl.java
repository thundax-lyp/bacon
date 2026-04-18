package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.repository.OrderAuditLogRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderAuditLogRepositoryImpl implements OrderAuditLogRepository {

    private final Map<String, List<OrderAuditLog>> auditLogStorage = new ConcurrentHashMap<>();

    @Override
    public void insert(OrderAuditLog auditLog) {
        String key = currentTenantId() + ":" + toOrderNoValue(auditLog.getOrderNo());
        auditLogStorage
                .computeIfAbsent(key, unused -> new java.util.ArrayList<>())
                .add(auditLog);
    }

    @Override
    public List<OrderAuditLog> listByOrderNo(OrderNo orderNo) {
        return List.copyOf(auditLogStorage.getOrDefault(currentTenantId() + ":" + toOrderNoValue(orderNo), List.of()));
    }

    private Long currentTenantId() {
        return BaconContextHolder.requireTenantId();
    }

    private String toOrderNoValue(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }
}
