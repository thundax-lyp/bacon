package com.github.thundax.bacon.payment.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.payment.application.audit.PaymentOperationLogSupport;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class PaymentCallbackApplicationService {

    private static final String PAYMENT_CALLBACK_RECORD_ID_BIZ_TAG = "payment_callback_record_id";

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackRecordRepository paymentCallbackRecordRepository;
    private final PaymentOperationLogSupport paymentOperationLogSupport;
    private final OrderCommandFacade orderCommandFacade;
    private final IdGenerator idGenerator;

    public PaymentCallbackApplicationService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentCallbackRecordRepository paymentCallbackRecordRepository,
            PaymentOperationLogSupport paymentOperationLogSupport,
            OrderCommandFacade orderCommandFacade,
            IdGenerator idGenerator) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentCallbackRecordRepository = paymentCallbackRecordRepository;
        this.paymentOperationLogSupport = paymentOperationLogSupport;
        this.orderCommandFacade = orderCommandFacade;
        this.idGenerator = idGenerator;
    }

    public void callbackPaid(
            String channelCode,
            String paymentNo,
            String channelTransactionNo,
            String channelStatus,
            String rawPayload) {
        BaconContextHolder.requireTenantId();
        validateChannel(channelCode);
        validateSuccessCallbackPayload(channelTransactionNo, channelStatus, rawPayload);
        PaymentOrder paymentOrder = paymentOrderRepository
                .findByPaymentNo(paymentNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo));
        PaymentCallbackRecord existing = paymentCallbackRecordRepository
                .findByChannelTransactionNo(channelCode, channelTransactionNo)
                .orElse(null);
        // 成功回调先按渠道交易号落幂等记录，再驱动主单状态；这样即使后续编排失败，也不会丢失渠道侧证据。
        PaymentCallbackRecord callbackRecord = existing == null
                ? paymentCallbackRecordRepository.insert(PaymentCallbackRecord.create(
                        idGenerator.nextId(PAYMENT_CALLBACK_RECORD_ID_BIZ_TAG),
                        PaymentNo.of(paymentNo),
                        paymentOrder.getOrderNo(),
                        PaymentChannelCode.fromValue(channelCode),
                        channelTransactionNo,
                        PaymentChannelStatus.fromValue(channelStatus),
                        rawPayload,
                        Instant.now()))
                : existing;
        // 已支付或已终态的单子不再重复改主单，只补审计，避免重复回调把最终状态重新覆盖。
        if (PaymentStatus.PAID == paymentOrder.getPaymentStatus()) {
            paymentOperationLogSupport.recordCallback(
                    PaymentAuditActionType.CALLBACK_PAID,
                    paymentNo,
                    paymentOrder.getPaymentStatus().value(),
                    paymentOrder.getPaymentStatus().value(),
                    Instant.now());
            return;
        }
        if (PaymentStatus.FAILED == paymentOrder.getPaymentStatus()
                || PaymentStatus.CLOSED == paymentOrder.getPaymentStatus()) {
            paymentOperationLogSupport.recordCallback(
                    PaymentAuditActionType.CALLBACK_PAID,
                    paymentNo,
                    paymentOrder.getPaymentStatus().value(),
                    paymentOrder.getPaymentStatus().value(),
                    Instant.now());
            return;
        }
        String beforeStatus = paymentOrder.getPaymentStatus().value();
        Instant paidTime = Instant.now();
        // 支付域以本单金额作为最终入账金额，不信任重复回调里可能变化的金额字段，减少渠道模拟数据带来的歧义。
        paymentOrder.markPaid(
                paymentOrder.getAmount(),
                paidTime,
                callbackRecord.getChannelTransactionNo(),
                callbackRecord.getChannelStatus(),
                callbackRecord.summarize());
        paymentOrderRepository.update(paymentOrder);
        paymentOperationLogSupport.recordCallback(
                PaymentAuditActionType.CALLBACK_PAID,
                paymentNo,
                beforeStatus,
                paymentOrder.getPaymentStatus().value(),
                paidTime);
        BaconContextHolder.runWithCurrentContext(() -> orderCommandFacade.markPaid(new OrderMarkPaidFacadeRequest(
                paymentOrder.getOrderNo().value(),
                paymentNo,
                channelCode,
                paymentOrder.getAmount().value(),
                paidTime)));
    }

    public void callbackFailed(
            String channelCode, String paymentNo, String channelStatus, String rawPayload, String reason) {
        BaconContextHolder.requireTenantId();
        validateChannel(channelCode);
        validateFailedCallbackPayload(channelStatus, rawPayload, reason);
        PaymentOrder paymentOrder = paymentOrderRepository
                .findByPaymentNo(paymentNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo));
        PaymentCallbackRecord latestRecord = paymentCallbackRecordRepository
                .findLatestByPaymentNo(paymentNo)
                .orElse(null);
        // 失败回调没有稳定的渠道交易号时，只能按“最近一条内容是否相同”去重，避免同一失败通知被无限累积。
        if (latestRecord == null
                || !channelStatus.equals(latestRecord.getChannelStatus().value())
                || !rawPayload.equals(latestRecord.getRawPayload())) {
            paymentCallbackRecordRepository.insert(PaymentCallbackRecord.create(
                    idGenerator.nextId(PAYMENT_CALLBACK_RECORD_ID_BIZ_TAG),
                    PaymentNo.of(paymentNo),
                    paymentOrder.getOrderNo(),
                    PaymentChannelCode.fromValue(channelCode),
                    null,
                    PaymentChannelStatus.fromValue(channelStatus),
                    rawPayload,
                    Instant.now()));
        }
        // 已支付、已失败或已关闭都视为终态，失败回调只记审计，不允许把终态主单重新拉回失败流程。
        if (PaymentStatus.PAID == paymentOrder.getPaymentStatus()) {
            paymentOperationLogSupport.recordCallback(
                    PaymentAuditActionType.CALLBACK_FAILED,
                    paymentNo,
                    paymentOrder.getPaymentStatus().value(),
                    paymentOrder.getPaymentStatus().value(),
                    Instant.now());
            return;
        }
        if (PaymentStatus.FAILED == paymentOrder.getPaymentStatus()
                || PaymentStatus.CLOSED == paymentOrder.getPaymentStatus()) {
            paymentOperationLogSupport.recordCallback(
                    PaymentAuditActionType.CALLBACK_FAILED,
                    paymentNo,
                    paymentOrder.getPaymentStatus().value(),
                    paymentOrder.getPaymentStatus().value(),
                    Instant.now());
            return;
        }
        String beforeStatus = paymentOrder.getPaymentStatus().value();
        Instant failedTime = Instant.now();
        // 回调摘要只保留截断后的原始载荷，避免把超长渠道报文直接写进主单，主单字段只承载查询常用摘要。
        paymentOrder.markFailed(
                PaymentChannelStatus.fromValue(channelStatus),
                rawPayload.length() <= 255 ? rawPayload : rawPayload.substring(0, 255));
        paymentOrderRepository.update(paymentOrder);
        paymentOperationLogSupport.recordCallback(
                PaymentAuditActionType.CALLBACK_FAILED,
                paymentNo,
                beforeStatus,
                paymentOrder.getPaymentStatus().value(),
                failedTime);
        BaconContextHolder.runWithCurrentContext(() -> orderCommandFacade.markPaymentFailed(
                new OrderMarkPaymentFailedFacadeRequest(
                        paymentOrder.getOrderNo().value(), paymentNo, reason, channelStatus, failedTime)));
    }

    private void validateChannel(String channelCode) {
        PaymentChannelCode.fromValue(channelCode);
    }

    private void validateSuccessCallbackPayload(String channelTransactionNo, String channelStatus, String rawPayload) {
        if (channelTransactionNo == null || channelTransactionNo.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "channelTransactionNo");
        }
        if (channelStatus == null || channelStatus.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "channelStatus");
        }
        PaymentChannelStatus.fromValue(channelStatus);
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "rawPayload");
        }
    }

    private void validateFailedCallbackPayload(String channelStatus, String rawPayload, String reason) {
        if (channelStatus == null || channelStatus.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "channelStatus");
        }
        PaymentChannelStatus.fromValue(channelStatus);
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "rawPayload");
        }
        if (reason == null || reason.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "reason");
        }
    }
}
