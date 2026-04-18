package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderAuditLogPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderAuditLogDO;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderAuditLogMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class OrderAuditLogRepositorySupport {

    private static final String AUDIT_LOG_ID_BIZ_TAG = "order_audit_log_id";

    private final OrderAuditLogMapper orderAuditLogMapper;
    private final OrderAuditLogPersistenceAssembler orderAuditLogPersistenceAssembler;
    private final IdGenerator idGenerator;

    public OrderAuditLogRepositorySupport(
            OrderAuditLogMapper orderAuditLogMapper,
            OrderAuditLogPersistenceAssembler orderAuditLogPersistenceAssembler,
            IdGenerator idGenerator) {
        this.orderAuditLogMapper = orderAuditLogMapper;
        this.orderAuditLogPersistenceAssembler = orderAuditLogPersistenceAssembler;
        this.idGenerator = idGenerator;
    }

    public void insert(OrderAuditLog auditLog) {
        OrderAuditLogDO dataObject = orderAuditLogPersistenceAssembler.toDataObject(auditLog);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(AUDIT_LOG_ID_BIZ_TAG));
        }
        orderAuditLogMapper.insert(dataObject);
    }

    public List<OrderAuditLog> listByOrderNo(OrderNo orderNo) {
        return orderAuditLogMapper
                .selectList(Wrappers.<OrderAuditLogDO>lambdaQuery()
                        .eq(OrderAuditLogDO::getOrderNo, orderNo == null ? null : orderNo.value())
                        .orderByAsc(OrderAuditLogDO::getOccurredAt, OrderAuditLogDO::getId))
                .stream()
                .map(orderAuditLogPersistenceAssembler::toDomain)
                .toList();
    }
}
