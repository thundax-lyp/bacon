package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.payment.application.support.PaymentAuditLogSupport;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentCallbackApplicationService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackRecordRepository paymentCallbackRecordRepository;
    private final PaymentAuditLogSupport paymentAuditLogSupport;
    private final OrderCommandFacade orderCommandFacade;

    public PaymentCallbackApplicationService(PaymentOrderRepository paymentOrderRepository,
                                             PaymentCallbackRecordRepository paymentCallbackRecordRepository,
                                             PaymentAuditLogSupport paymentAuditLogSupport,
                                             OrderCommandFacade orderCommandFacade) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentCallbackRecordRepository = paymentCallbackRecordRepository;
        this.paymentAuditLogSupport = paymentAuditLogSupport;
        this.orderCommandFacade = orderCommandFacade;
    }

    public void callbackPaid(String channelCode, Long tenantId, String paymentNo, String channelTransactionNo,
                             String channelStatus, String rawPayload) {
        validateChannel(channelCode);
        validateSuccessCallbackPayload(channelTransactionNo, channelStatus, rawPayload);
        PaymentOrder paymentOrder = paymentOrderRepository.findOrderByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo));
        PaymentCallbackRecord existing = paymentCallbackRecordRepository
                .findCallbackByChannelTransactionNo(tenantId, channelCode, channelTransactionNo)
                .orElse(null);
        PaymentCallbackRecord callbackRecord = existing == null
                ? paymentCallbackRecordRepository.save(new PaymentCallbackRecord(null, tenantId,
                paymentNo, paymentOrder.getOrderNo(), channelCode, channelTransactionNo, channelStatus, rawPayload, Instant.now()))
                : existing;
        if (PaymentOrder.STATUS_PAID.equals(paymentOrder.getPaymentStatus())) {
            recordCallbackAudit(PaymentAuditLog.ACTION_CALLBACK_PAID, tenantId, paymentNo,
                    paymentOrder.getPaymentStatus(), paymentOrder.getPaymentStatus(), Instant.now());
            return;
        }
        if (PaymentOrder.STATUS_FAILED.equals(paymentOrder.getPaymentStatus())
                || PaymentOrder.STATUS_CLOSED.equals(paymentOrder.getPaymentStatus())) {
            recordCallbackAudit(PaymentAuditLog.ACTION_CALLBACK_PAID, tenantId, paymentNo,
                    paymentOrder.getPaymentStatus(), paymentOrder.getPaymentStatus(), Instant.now());
            return;
        }
        String beforeStatus = paymentOrder.getPaymentStatus();
        Instant paidTime = Instant.now();
        paymentOrder.markPaid(paymentOrder.getAmount(), paidTime, callbackRecord.getChannelTransactionNo(),
                callbackRecord.getChannelStatus(), callbackRecord.summarize());
        paymentOrderRepository.save(paymentOrder);
        paymentAuditLogSupport.saveSafely(new PaymentAuditLog(null, tenantId, paymentNo,
                PaymentAuditLog.ACTION_CALLBACK_PAID, beforeStatus, paymentOrder.getPaymentStatus(),
                PaymentAuditLog.OPERATOR_CHANNEL, 0L, paidTime));
        orderCommandFacade.markPaid(tenantId, paymentOrder.getOrderNo(), paymentNo, channelCode,
                paymentOrder.getAmount(), paidTime);
    }

    public void callbackFailed(String channelCode, Long tenantId, String paymentNo, String channelStatus,
                               String rawPayload, String reason) {
        validateChannel(channelCode);
        validateFailedCallbackPayload(channelStatus, rawPayload, reason);
        PaymentOrder paymentOrder = paymentOrderRepository.findOrderByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo));
        PaymentCallbackRecord latestRecord = paymentCallbackRecordRepository
                .findLatestCallbackByPaymentNo(tenantId, paymentNo)
                .orElse(null);
        if (latestRecord == null
                || !channelStatus.equals(latestRecord.getChannelStatus())
                || !rawPayload.equals(latestRecord.getRawPayload())) {
            paymentCallbackRecordRepository.save(new PaymentCallbackRecord(null, tenantId,
                    paymentNo, paymentOrder.getOrderNo(), channelCode, null, channelStatus, rawPayload, Instant.now()));
        }
        if (PaymentOrder.STATUS_PAID.equals(paymentOrder.getPaymentStatus())) {
            recordCallbackAudit(PaymentAuditLog.ACTION_CALLBACK_FAILED, tenantId, paymentNo,
                    paymentOrder.getPaymentStatus(), paymentOrder.getPaymentStatus(), Instant.now());
            return;
        }
        if (PaymentOrder.STATUS_FAILED.equals(paymentOrder.getPaymentStatus())
                || PaymentOrder.STATUS_CLOSED.equals(paymentOrder.getPaymentStatus())) {
            recordCallbackAudit(PaymentAuditLog.ACTION_CALLBACK_FAILED, tenantId, paymentNo,
                    paymentOrder.getPaymentStatus(), paymentOrder.getPaymentStatus(), Instant.now());
            return;
        }
        String beforeStatus = paymentOrder.getPaymentStatus();
        Instant failedTime = Instant.now();
        paymentOrder.markFailed(channelStatus, rawPayload.length() <= 255 ? rawPayload : rawPayload.substring(0, 255));
        paymentOrderRepository.save(paymentOrder);
        paymentAuditLogSupport.saveSafely(new PaymentAuditLog(null, tenantId, paymentNo,
                PaymentAuditLog.ACTION_CALLBACK_FAILED, beforeStatus, paymentOrder.getPaymentStatus(),
                PaymentAuditLog.OPERATOR_CHANNEL, 0L, failedTime));
        orderCommandFacade.markPaymentFailed(tenantId, paymentOrder.getOrderNo(), paymentNo, reason, channelStatus, failedTime);
    }

    private void validateChannel(String channelCode) {
        if (!PaymentOrder.CHANNEL_MOCK.equals(channelCode)) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CHANNEL_CODE, channelCode);
        }
    }

    private void validateSuccessCallbackPayload(String channelTransactionNo, String channelStatus, String rawPayload) {
        if (channelTransactionNo == null || channelTransactionNo.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "channelTransactionNo");
        }
        if (channelStatus == null || channelStatus.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "channelStatus");
        }
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "rawPayload");
        }
    }

    private void validateFailedCallbackPayload(String channelStatus, String rawPayload, String reason) {
        if (channelStatus == null || channelStatus.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "channelStatus");
        }
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "rawPayload");
        }
        if (reason == null || reason.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CALLBACK_REQUEST, "reason");
        }
    }

    private void recordCallbackAudit(String actionType, Long tenantId, String paymentNo, String beforeStatus,
                                     String afterStatus, Instant occurredAt) {
        paymentAuditLogSupport.saveSafely(new PaymentAuditLog(null, tenantId, paymentNo, actionType,
                beforeStatus, afterStatus, PaymentAuditLog.OPERATOR_CHANNEL, 0L, occurredAt));
    }
}
