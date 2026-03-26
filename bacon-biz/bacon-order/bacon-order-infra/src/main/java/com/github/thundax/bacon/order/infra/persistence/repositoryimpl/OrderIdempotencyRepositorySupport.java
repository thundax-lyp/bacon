package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderIdempotencyRecordDataObject;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderIdempotencyRecordMapper;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.order.repository.mode", havingValue = "strict", matchIfMissing = true)
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
public class OrderIdempotencyRepositorySupport {

    private static final int LAST_ERROR_MAX_LENGTH = 512;

    private final OrderIdempotencyRecordMapper mapper;

    public OrderIdempotencyRepositorySupport(OrderIdempotencyRecordMapper mapper) {
        this.mapper = mapper;
    }

    public boolean createProcessing(OrderIdempotencyRecord record) {
        OrderIdempotencyRecordDataObject dataObject = toDataObject(record);
        Instant now = Instant.now();
        dataObject.setStatus(OrderIdempotencyRecord.STATUS_PROCESSING);
        dataObject.setAttemptCount(dataObject.getAttemptCount() == null ? 1 : dataObject.getAttemptCount());
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        dataObject.setLastError(null);
        try {
            mapper.insert(dataObject);
            record.setId(dataObject.getId());
            record.setStatus(dataObject.getStatus());
            record.setAttemptCount(dataObject.getAttemptCount());
            record.setCreatedAt(dataObject.getCreatedAt());
            record.setUpdatedAt(dataObject.getUpdatedAt());
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    public Optional<OrderIdempotencyRecord> findByBusinessKey(Long tenantId, String orderNo, String paymentNo,
                                                               String eventType) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<OrderIdempotencyRecordDataObject>lambdaQuery()
                        .eq(OrderIdempotencyRecordDataObject::getTenantId, tenantId)
                        .eq(OrderIdempotencyRecordDataObject::getOrderNo, orderNo)
                        .eq(OrderIdempotencyRecordDataObject::getPaymentNo, paymentNo)
                        .eq(OrderIdempotencyRecordDataObject::getEventType, eventType)))
                .map(this::toDomain);
    }

    public boolean markSuccess(Long tenantId, String orderNo, String paymentNo, String eventType, Instant updatedAt) {
        return mapper.update(null, Wrappers.<OrderIdempotencyRecordDataObject>lambdaUpdate()
                .eq(OrderIdempotencyRecordDataObject::getTenantId, tenantId)
                .eq(OrderIdempotencyRecordDataObject::getOrderNo, orderNo)
                .eq(OrderIdempotencyRecordDataObject::getPaymentNo, paymentNo)
                .eq(OrderIdempotencyRecordDataObject::getEventType, eventType)
                .eq(OrderIdempotencyRecordDataObject::getStatus, OrderIdempotencyRecord.STATUS_PROCESSING)
                .set(OrderIdempotencyRecordDataObject::getStatus, OrderIdempotencyRecord.STATUS_SUCCESS)
                .set(OrderIdempotencyRecordDataObject::getLastError, null)
                .set(OrderIdempotencyRecordDataObject::getUpdatedAt, updatedAt)) > 0;
    }

    public boolean markFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                              String lastError, Instant updatedAt) {
        return mapper.update(null, Wrappers.<OrderIdempotencyRecordDataObject>lambdaUpdate()
                .eq(OrderIdempotencyRecordDataObject::getTenantId, tenantId)
                .eq(OrderIdempotencyRecordDataObject::getOrderNo, orderNo)
                .eq(OrderIdempotencyRecordDataObject::getPaymentNo, paymentNo)
                .eq(OrderIdempotencyRecordDataObject::getEventType, eventType)
                .eq(OrderIdempotencyRecordDataObject::getStatus, OrderIdempotencyRecord.STATUS_PROCESSING)
                .set(OrderIdempotencyRecordDataObject::getStatus, OrderIdempotencyRecord.STATUS_FAILED)
                .set(OrderIdempotencyRecordDataObject::getLastError, truncate(lastError))
                .set(OrderIdempotencyRecordDataObject::getUpdatedAt, updatedAt)) > 0;
    }

    public boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType, Instant updatedAt) {
        return mapper.update(null, Wrappers.<OrderIdempotencyRecordDataObject>lambdaUpdate()
                .eq(OrderIdempotencyRecordDataObject::getTenantId, tenantId)
                .eq(OrderIdempotencyRecordDataObject::getOrderNo, orderNo)
                .eq(OrderIdempotencyRecordDataObject::getPaymentNo, paymentNo)
                .eq(OrderIdempotencyRecordDataObject::getEventType, eventType)
                .eq(OrderIdempotencyRecordDataObject::getStatus, OrderIdempotencyRecord.STATUS_FAILED)
                .set(OrderIdempotencyRecordDataObject::getStatus, OrderIdempotencyRecord.STATUS_PROCESSING)
                .setSql("attempt_count = attempt_count + 1")
                .set(OrderIdempotencyRecordDataObject::getLastError, null)
                .set(OrderIdempotencyRecordDataObject::getUpdatedAt, updatedAt)) > 0;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= LAST_ERROR_MAX_LENGTH ? value : value.substring(0, LAST_ERROR_MAX_LENGTH);
    }

    private OrderIdempotencyRecordDataObject toDataObject(OrderIdempotencyRecord record) {
        return new OrderIdempotencyRecordDataObject(record.getId(), record.getTenantId(), record.getOrderNo(),
                normalizePaymentNo(record.getPaymentNo()), record.getEventType(), record.getStatus(),
                record.getAttemptCount(), record.getLastError(), record.getCreatedAt(), record.getUpdatedAt());
    }

    private OrderIdempotencyRecord toDomain(OrderIdempotencyRecordDataObject dataObject) {
        return new OrderIdempotencyRecord(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getPaymentNo(), dataObject.getEventType(), dataObject.getStatus(),
                dataObject.getAttemptCount(), dataObject.getLastError(), dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }

    private String normalizePaymentNo(String paymentNo) {
        return paymentNo == null ? "" : paymentNo;
    }
}
