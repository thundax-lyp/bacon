package com.github.thundax.bacon.payment.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentAuditLogDO;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentCallbackRecordDO;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentOrderDO;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentAuditLogMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentCallbackRecordMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentOrderMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "bacon.payment.repository.mode", havingValue = "strict", matchIfMissing = true)
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
public class PaymentRepositorySupport {

    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentCallbackRecordMapper paymentCallbackRecordMapper;
    private final PaymentAuditLogMapper paymentAuditLogMapper;

    public PaymentRepositorySupport(PaymentOrderMapper paymentOrderMapper,
                                    PaymentCallbackRecordMapper paymentCallbackRecordMapper,
                                    PaymentAuditLogMapper paymentAuditLogMapper) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.paymentCallbackRecordMapper = paymentCallbackRecordMapper;
        this.paymentAuditLogMapper = paymentAuditLogMapper;
        log.info("Using MyBatis-Plus payment repository");
    }

    public PaymentOrder saveOrder(PaymentOrder paymentOrder) {
        Instant now = Instant.now();
        PaymentOrderDO dataObject = toDataObject(paymentOrder, now);
        if (dataObject.getId() == null) {
            paymentOrderMapper.insert(dataObject);
        } else {
            if (paymentOrderMapper.updateById(dataObject) == 0) {
                throw new PaymentDomainException(PaymentErrorCode.PAYMENT_PERSISTENCE_CONFLICT,
                        paymentOrder.getPaymentNo());
            }
        }
        return toDomain(dataObject);
    }

    public Optional<PaymentOrder> findOrderByPaymentNo(Long tenantId, String paymentNo) {
        return Optional.ofNullable(paymentOrderMapper.selectOne(Wrappers.<PaymentOrderDO>lambdaQuery()
                        .eq(PaymentOrderDO::getTenantId, tenantId)
                        .eq(PaymentOrderDO::getPaymentNo, paymentNo)))
                .map(this::toDomain);
    }

    public Optional<PaymentOrder> findOrderByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(paymentOrderMapper.selectOne(Wrappers.<PaymentOrderDO>lambdaQuery()
                        .eq(PaymentOrderDO::getTenantId, tenantId)
                        .eq(PaymentOrderDO::getOrderNo, orderNo)))
                .map(this::toDomain);
    }

    public PaymentCallbackRecord saveCallbackRecord(PaymentCallbackRecord callbackRecord) {
        PaymentCallbackRecordDO dataObject = toDataObject(callbackRecord);
        if (dataObject.getId() == null) {
            paymentCallbackRecordMapper.insert(dataObject);
        } else {
            paymentCallbackRecordMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(Long tenantId, String paymentNo) {
        return paymentCallbackRecordMapper.selectList(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getTenantId, tenantId)
                        .eq(PaymentCallbackRecordDO::getPaymentNo, paymentNo)
                        .orderByDesc(PaymentCallbackRecordDO::getReceivedAt, PaymentCallbackRecordDO::getId)
                        .last("limit 1"))
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(Long tenantId, String channelCode,
                                                                              String channelTransactionNo) {
        return Optional.ofNullable(paymentCallbackRecordMapper.selectOne(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getTenantId, tenantId)
                        .eq(PaymentCallbackRecordDO::getChannelCode, channelCode)
                        .eq(PaymentCallbackRecordDO::getChannelTransactionNo, channelTransactionNo)))
                .map(this::toDomain);
    }

    public List<PaymentCallbackRecord> findCallbacksByPaymentNo(Long tenantId, String paymentNo) {
        return paymentCallbackRecordMapper.selectList(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getTenantId, tenantId)
                        .eq(PaymentCallbackRecordDO::getPaymentNo, paymentNo)
                        .orderByDesc(PaymentCallbackRecordDO::getReceivedAt, PaymentCallbackRecordDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public void saveAuditLog(PaymentAuditLog auditLog) {
        PaymentAuditLogDO dataObject = toDataObject(auditLog);
        if (dataObject.getId() == null) {
            paymentAuditLogMapper.insert(dataObject);
            return;
        }
        paymentAuditLogMapper.updateById(dataObject);
    }

    public List<PaymentAuditLog> findAuditLogsByPaymentNo(Long tenantId, String paymentNo) {
        return paymentAuditLogMapper.selectList(Wrappers.<PaymentAuditLogDO>lambdaQuery()
                        .eq(PaymentAuditLogDO::getTenantId, tenantId)
                        .eq(PaymentAuditLogDO::getPaymentNo, paymentNo)
                        .orderByAsc(PaymentAuditLogDO::getOccurredAt, PaymentAuditLogDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PaymentOrderDO toDataObject(PaymentOrder paymentOrder, Instant now) {
        return new PaymentOrderDO(paymentOrder.getId(), paymentOrder.getTenantId(), paymentOrder.getPaymentNo(),
                paymentOrder.getOrderNo(), paymentOrder.getUserId(), paymentOrder.getChannelCode(),
                paymentOrder.getPaymentStatus(), paymentOrder.getAmount(), paymentOrder.getPaidAmount(),
                paymentOrder.getSubject(), paymentOrder.getCreatedAt(), now, paymentOrder.getExpiredAt(),
                paymentOrder.getPaidAt(), paymentOrder.getClosedAt());
    }

    private PaymentOrder toDomain(PaymentOrderDO dataObject) {
        return PaymentOrder.rehydrate(dataObject.getId(), dataObject.getTenantId(), dataObject.getPaymentNo(),
                dataObject.getOrderNo(), dataObject.getUserId(), dataObject.getChannelCode(),
                dataObject.getAmount(), dataObject.getPaidAmount(), dataObject.getSubject(), dataObject.getCreatedAt(),
                dataObject.getExpiredAt(), dataObject.getPaidAt(), dataObject.getClosedAt(), dataObject.getPaymentStatus(),
                null, null, null);
    }

    private PaymentCallbackRecordDO toDataObject(PaymentCallbackRecord callbackRecord) {
        return new PaymentCallbackRecordDO(callbackRecord.getId(), callbackRecord.getTenantId(),
                callbackRecord.getPaymentNo(), callbackRecord.getOrderNo(), callbackRecord.getChannelCode(),
                callbackRecord.getChannelTransactionNo(), callbackRecord.getChannelStatus(),
                callbackRecord.getRawPayload(), callbackRecord.getReceivedAt());
    }

    private PaymentCallbackRecord toDomain(PaymentCallbackRecordDO dataObject) {
        return new PaymentCallbackRecord(dataObject.getId(), dataObject.getTenantId(), dataObject.getPaymentNo(),
                dataObject.getOrderNo(), dataObject.getChannelCode(), dataObject.getChannelTransactionNo(),
                dataObject.getChannelStatus(), dataObject.getRawPayload(), dataObject.getReceivedAt());
    }

    private PaymentAuditLogDO toDataObject(PaymentAuditLog auditLog) {
        return new PaymentAuditLogDO(auditLog.getId(), auditLog.getTenantId(), auditLog.getPaymentNo(),
                auditLog.getActionType(), auditLog.getBeforeStatus(), auditLog.getAfterStatus(),
                auditLog.getOperatorType(), auditLog.getOperatorId(), auditLog.getOccurredAt());
    }

    private PaymentAuditLog toDomain(PaymentAuditLogDO dataObject) {
        return new PaymentAuditLog(dataObject.getId(), dataObject.getTenantId(), dataObject.getPaymentNo(),
                dataObject.getActionType(), dataObject.getBeforeStatus(), dataObject.getAfterStatus(),
                dataObject.getOperatorType(), dataObject.getOperatorId(), dataObject.getOccurredAt());
    }
}
