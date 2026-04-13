package com.github.thundax.bacon.payment.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.infra.persistence.assembler.PaymentAuditLogPersistenceAssembler;
import com.github.thundax.bacon.payment.infra.persistence.assembler.PaymentCallbackRecordPersistenceAssembler;
import com.github.thundax.bacon.payment.infra.persistence.assembler.PaymentOrderPersistenceAssembler;
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

    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentCallbackRecordMapper paymentCallbackRecordMapper;
    private final PaymentAuditLogMapper paymentAuditLogMapper;
    private final IdGenerator idGenerator;
    private final PaymentOrderPersistenceAssembler paymentOrderPersistenceAssembler;
    private final PaymentCallbackRecordPersistenceAssembler paymentCallbackRecordPersistenceAssembler;
    private final PaymentAuditLogPersistenceAssembler paymentAuditLogPersistenceAssembler;

    public PaymentRepositorySupport(
            PaymentOrderMapper paymentOrderMapper,
            PaymentCallbackRecordMapper paymentCallbackRecordMapper,
            PaymentAuditLogMapper paymentAuditLogMapper,
            IdGenerator idGenerator,
            PaymentOrderPersistenceAssembler paymentOrderPersistenceAssembler,
            PaymentCallbackRecordPersistenceAssembler paymentCallbackRecordPersistenceAssembler,
            PaymentAuditLogPersistenceAssembler paymentAuditLogPersistenceAssembler) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.paymentCallbackRecordMapper = paymentCallbackRecordMapper;
        this.paymentAuditLogMapper = paymentAuditLogMapper;
        this.idGenerator = idGenerator;
        this.paymentOrderPersistenceAssembler = paymentOrderPersistenceAssembler;
        this.paymentCallbackRecordPersistenceAssembler = paymentCallbackRecordPersistenceAssembler;
        this.paymentAuditLogPersistenceAssembler = paymentAuditLogPersistenceAssembler;
        log.info("Using MyBatis-Plus payment repository");
    }

    public PaymentOrder saveOrder(PaymentOrder paymentOrder) {
        Instant now = Instant.now();
        PaymentOrderDO dataObject = paymentOrderPersistenceAssembler.toDataObject(paymentOrder, now);
        if (dataObject.getId() == null) {
            throw new IllegalArgumentException("payment order id must not be null");
        }
        PaymentOrderDO existing = paymentOrderMapper.selectById(dataObject.getId());
        if (existing == null) {
            paymentOrderMapper.insert(dataObject);
        } else {
            // 更新 0 行直接视为持久化冲突，避免应用层把“对象已保存”误判成成功。
            if (paymentOrderMapper.updateById(dataObject) == 0) {
                throw new PaymentDomainException(
                        PaymentErrorCode.PAYMENT_PERSISTENCE_CONFLICT,
                        paymentOrder.getPaymentNo().value());
            }
        }
        return paymentOrderPersistenceAssembler.toDomain(dataObject);
    }

    public Optional<PaymentOrder> findOrderByPaymentNo(String paymentNo) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(paymentOrderMapper.selectOne(
                        Wrappers.<PaymentOrderDO>lambdaQuery().eq(PaymentOrderDO::getPaymentNo, paymentNo)))
                .map(paymentOrderPersistenceAssembler::toDomain);
    }

    public Optional<PaymentOrder> findOrderByOrderNo(String orderNo) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(paymentOrderMapper.selectOne(
                        Wrappers.<PaymentOrderDO>lambdaQuery().eq(PaymentOrderDO::getOrderNo, orderNo)))
                .map(paymentOrderPersistenceAssembler::toDomain);
    }

    public PaymentCallbackRecord saveCallbackRecord(PaymentCallbackRecord callbackRecord) {
        PaymentCallbackRecordDO dataObject = paymentCallbackRecordPersistenceAssembler.toDataObject(callbackRecord);
        // 回调记录是证据追加模型；带上上游生成的 id 后直接插入，不在仓储层做“空 id 补发号”。
        if (dataObject.getId() == null) {
            throw new IllegalArgumentException("payment callback record id must not be null");
        }
        paymentCallbackRecordMapper.insert(dataObject);
        return paymentCallbackRecordPersistenceAssembler.toDomain(dataObject);
    }

    public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(String paymentNo) {
        BaconContextHolder.requireTenantId();
        // “最新回调”按 receivedAt + id 倒序取一条，用于查询兜底补全，而不是主单最终状态来源。
        return paymentCallbackRecordMapper
                .selectList(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getPaymentNo, paymentNo)
                        .orderByDesc(PaymentCallbackRecordDO::getReceivedAt, PaymentCallbackRecordDO::getId)
                        .last("limit 1"))
                .stream()
                .findFirst()
                .map(paymentCallbackRecordPersistenceAssembler::toDomain);
    }

    public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(
            String channelCode, String channelTransactionNo) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(paymentCallbackRecordMapper.selectOne(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getChannelCode, channelCode)
                        .eq(PaymentCallbackRecordDO::getChannelTransactionNo, channelTransactionNo)))
                .map(paymentCallbackRecordPersistenceAssembler::toDomain);
    }

    public List<PaymentCallbackRecord> findCallbacksByPaymentNo(String paymentNo) {
        BaconContextHolder.requireTenantId();
        return paymentCallbackRecordMapper
                .selectList(Wrappers.<PaymentCallbackRecordDO>lambdaQuery()
                        .eq(PaymentCallbackRecordDO::getPaymentNo, paymentNo)
                        .orderByDesc(PaymentCallbackRecordDO::getReceivedAt, PaymentCallbackRecordDO::getId))
                .stream()
                .map(paymentCallbackRecordPersistenceAssembler::toDomain)
                .toList();
    }

    public void saveAuditLog(PaymentAuditLog auditLog) {
        PaymentAuditLogDO dataObject = paymentAuditLogPersistenceAssembler.toDataObject(auditLog);
        // 支付审计 id 由上游发号后带入，这里只负责追加落库，不再在仓储层补发 id。
        if (dataObject.getId() == null) {
            throw new IllegalArgumentException("payment audit log id must not be null");
        }
        paymentAuditLogMapper.insert(dataObject);
    }

    public List<PaymentAuditLog> findAuditLogsByPaymentNo(String paymentNo) {
        BaconContextHolder.requireTenantId();
        return paymentAuditLogMapper
                .selectList(Wrappers.<PaymentAuditLogDO>lambdaQuery()
                        .eq(PaymentAuditLogDO::getPaymentNo, paymentNo)
                        .orderByAsc(PaymentAuditLogDO::getOccurredAt, PaymentAuditLogDO::getId))
                .stream()
                .map(paymentAuditLogPersistenceAssembler::toDomain)
                .toList();
    }
}
