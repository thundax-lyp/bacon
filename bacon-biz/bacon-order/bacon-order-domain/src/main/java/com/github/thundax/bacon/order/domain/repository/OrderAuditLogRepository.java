package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import java.util.List;

public interface OrderAuditLogRepository {

    void insert(OrderAuditLog auditLog);

    List<OrderAuditLog> listByOrderNo(OrderNo orderNo);
}
