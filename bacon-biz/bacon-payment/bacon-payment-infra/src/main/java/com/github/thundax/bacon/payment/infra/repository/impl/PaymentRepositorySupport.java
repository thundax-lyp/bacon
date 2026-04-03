package com.github.thundax.bacon.payment.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.PaymentOrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentNo;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentAuditLogDO;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentCallbackRecordDO;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentOrderDO;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentAuditLogMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentCallbackRecordMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentOrderMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class PaymentRepositorySupport {

    private static final String PAYMENT_CALLBACK_RECORD_ID_BIZ_TAG = "payment_callback_record_id";
    private static final String PAYMENT_AUDIT_LOG_ID_BIZ_TAG = "payment_audit_log_id";

    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentCallbackRecordMapper paymentCallbackRecordMapper;
    private final PaymentAuditLogMapper paymentAuditLogMapper;
    private final IdGenerator idGenerator;

    public PaymentRepositorySupport(PaymentOrderMapper paymentOrderMapper,
                                    PaymentCallbackRecordMapper paymentCallbackRecordMapper,
                                    PaymentAuditLogMapper paymentAuditLogMapper,
                                    IdGenerator idGenerator) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.paymentCallbackRecordMapper = paymentCallbackRecordMapper;
        this.paymentAuditLogMapper = paymentAuditLogMapper;
        this.idGenerator = idGenerator;
        log.info("Using MyBatis-Plus payment repository");
    }

    public PaymentOrder saveOrder(PaymentOrder paymentOrder) {
        Instant now = Instant.now();
        PaymentOrderDO dataObject = toDataObject(paymentOrder, now);
        if (dataObject.getId() == null) {
            paymentOrderMapper.insert(dataObject);
        } else {
            // 更新 0 行直接视为持久化冲突，避免应用层把“对象已保存”误判成成功。
            if (paymentOrderMapper.updateById(dataObject) == 0) {
                throw new PaymentDomainException(PaymentErrorCode.PAYMENT_PERSISTENCE_CONFLICT,
                        paymentOrder.getPaymentNo().value());
            }
        }
        return toDomain(dataObject);
    }

    public Optional<PaymentOrder> findOrderByPaymentNo(Long tenantId, String paymentNo) {
        return Optional.ofNullable(paymentOrderMapper.selectOne(Wrappers.<PaymentOrderDO>lambdaQuery()
                        .eq(PaymentOrderDO::getTenantId, String.valueOf(tenantId))
                        .eq(PaymentOrderDO::getPaymentNo, paymentNo)))
                .map(this::toDomain);
    }

    public Optional<PaymentOrder> findOrderByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(paymentOrderMapper.selectOne(Wrappers.<PaymentOrderDO>lambdaQuery()
                        .eq(PaymentOrderDO::getTenantId, String.valueOf(tenantId))
                        .eq(PaymentOrderDO::getOrderNo, orderNo)))
                .map(this::toDomain);
    }

    public PaymentCallbackRecord saveCallbackRecord(PaymentCallbackRecord callbackRecord) {
        PaymentCallbackRecordDO dataObject = toDataObject(callbackRecord);
        // 回调记录以追加为主；只有明确带 id 的场景才走更新，避免正常回调把历史证据覆盖掉。
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(PAYMENT_CALLBACK_RECORD_ID_BIZ_TAG));
            paymentCallbackRecordMapper.insert(dataObject);
        } else {
            paymentCallbackRecordMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(Long tenantId, String paymentNo) {
        // “最新回调”按 receivedAt + id 倒序取一条，用于查询兜底补全，而不是主单最终状态来源。
        return paymentCallbackRecordMapper.selectList(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getTenantId, String.valueOf(tenantId))
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
                        .eq(PaymentCallbackRecordDO::getTenantId, String.valueOf(tenantId))
                        .eq(PaymentCallbackRecordDO::getChannelCode, channelCode)
                        .eq(PaymentCallbackRecordDO::getChannelTransactionNo, channelTransactionNo)))
                .map(this::toDomain);
    }

    public List<PaymentCallbackRecord> findCallbacksByPaymentNo(Long tenantId, String paymentNo) {
        return paymentCallbackRecordMapper.selectList(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getTenantId, String.valueOf(tenantId))
                        .eq(PaymentCallbackRecordDO::getPaymentNo, paymentNo)
                        .orderByDesc(PaymentCallbackRecordDO::getReceivedAt, PaymentCallbackRecordDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public void saveAuditLog(PaymentAuditLog auditLog) {
        PaymentAuditLogDO dataObject = toDataObject(auditLog);
        // 支付审计正常情况下只追加；保留 updateById 只是为了兼容少量测试或补数据场景。
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(PAYMENT_AUDIT_LOG_ID_BIZ_TAG));
            paymentAuditLogMapper.insert(dataObject);
            return;
        }
        paymentAuditLogMapper.updateById(dataObject);
    }

    public List<PaymentAuditLog> findAuditLogsByPaymentNo(Long tenantId, String paymentNo) {
        return paymentAuditLogMapper.selectList(Wrappers.<PaymentAuditLogDO>lambdaQuery()
                        .eq(PaymentAuditLogDO::getTenantId, String.valueOf(tenantId))
                        .eq(PaymentAuditLogDO::getPaymentNo, paymentNo)
                        .orderByAsc(PaymentAuditLogDO::getOccurredAt, PaymentAuditLogDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PaymentOrderDO toDataObject(PaymentOrder paymentOrder, Instant now) {
        // strict 持久化模型里，主表只固化主单核心字段；渠道交易号、回调摘要等证据留在回调表。
        return new PaymentOrderDO(toDatabaseId(paymentOrder.getId()), toDatabaseTenantId(paymentOrder.getTenantId()),
                paymentOrder.getPaymentNo().value(),
                paymentOrder.getOrderNo().value(), toDatabaseUserId(paymentOrder.getUserId()), paymentOrder.getChannelCode().value(),
                paymentOrder.getPaymentStatus().value(), paymentOrder.getAmount().value(),
                paymentOrder.getPaidAmount().value(),
                paymentOrder.getSubject(), paymentOrder.getCreatedAt(), now, paymentOrder.getExpiredAt(),
                paymentOrder.getPaidAt(), paymentOrder.getClosedAt());
    }

    private PaymentOrder toDomain(PaymentOrderDO dataObject) {
        // rehydrate 主单时不会从主表反填渠道回调细节，查询层需要时再结合 callback record 补足展示信息。
        return PaymentOrder.rehydrate(toDomainId(dataObject.getId()), toDomainTenantId(dataObject.getTenantId()),
                toPaymentNo(dataObject.getPaymentNo()),
                toOrderNo(dataObject.getOrderNo()), toDomainUserId(dataObject.getUserId()),
                PaymentChannelCode.fromValue(dataObject.getChannelCode()),
                Money.of(dataObject.getAmount()), toMoney(dataObject.getPaidAmount()), dataObject.getSubject(),
                dataObject.getCreatedAt(), dataObject.getExpiredAt(), dataObject.getPaidAt(), dataObject.getClosedAt(),
                PaymentStatus.fromValue(dataObject.getPaymentStatus()),
                null, null, null);
    }

    private Money toMoney(java.math.BigDecimal value) {
        return value == null ? Money.zero() : Money.of(value);
    }

    private Long toDatabaseId(PaymentOrderId paymentOrderId) {
        return paymentOrderId == null ? null : Long.valueOf(paymentOrderId.value());
    }

    private PaymentOrderId toDomainId(Long id) {
        return id == null ? null : PaymentOrderId.of(String.valueOf(id));
    }

    private String toDatabaseTenantId(TenantId tenantId) {
        return tenantId == null ? null : tenantId.value();
    }

    private TenantId toDomainTenantId(String tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private String toDatabaseUserId(UserId userId) {
        return userId == null ? null : userId.value();
    }

    private UserId toDomainUserId(String userId) {
        return userId == null ? null : UserId.of(userId);
    }

    private PaymentNo toPaymentNo(String paymentNo) {
        return paymentNo == null ? null : PaymentNo.of(paymentNo);
    }

    private OrderNo toOrderNo(String orderNo) {
        return orderNo == null ? null : OrderNo.of(orderNo);
    }

    private PaymentCallbackRecordDO toDataObject(PaymentCallbackRecord callbackRecord) {
        return new PaymentCallbackRecordDO(callbackRecord.getId(), String.valueOf(callbackRecord.getTenantId()),
                callbackRecord.getPaymentNo(), callbackRecord.getOrderNo(), callbackRecord.getChannelCode(),
                callbackRecord.getChannelTransactionNo(), callbackRecord.getChannelStatus(),
                callbackRecord.getRawPayload(), callbackRecord.getReceivedAt());
    }

    private PaymentCallbackRecord toDomain(PaymentCallbackRecordDO dataObject) {
        return new PaymentCallbackRecord(dataObject.getId(), Long.valueOf(dataObject.getTenantId()), dataObject.getPaymentNo(),
                dataObject.getOrderNo(), dataObject.getChannelCode(), dataObject.getChannelTransactionNo(),
                dataObject.getChannelStatus(), dataObject.getRawPayload(), dataObject.getReceivedAt());
    }

    private PaymentAuditLogDO toDataObject(PaymentAuditLog auditLog) {
        return new PaymentAuditLogDO(auditLog.getId(), toDatabaseTenantId(auditLog.getTenantId()), auditLog.getPaymentNo(),
                auditLog.getActionType(), auditLog.getBeforeStatus(), auditLog.getAfterStatus(),
                auditLog.getOperatorType(), toDatabaseOperatorId(auditLog.getOperatorId()), auditLog.getOccurredAt());
    }

    private PaymentAuditLog toDomain(PaymentAuditLogDO dataObject) {
        return new PaymentAuditLog(dataObject.getId(), toDomainTenantId(dataObject.getTenantId()), dataObject.getPaymentNo(),
                dataObject.getActionType(), dataObject.getBeforeStatus(), dataObject.getAfterStatus(),
                dataObject.getOperatorType(), toDomainOperatorId(dataObject.getOperatorId()), dataObject.getOccurredAt());
    }

    private String toDatabaseOperatorId(Long operatorId) {
        return operatorId == null ? null : String.valueOf(operatorId);
    }

    private Long toDomainOperatorId(String operatorId) {
        return operatorId == null ? null : Long.valueOf(operatorId);
    }
}
