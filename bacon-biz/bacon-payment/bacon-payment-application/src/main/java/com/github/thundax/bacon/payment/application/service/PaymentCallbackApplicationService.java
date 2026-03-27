package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentCallbackApplicationService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackRecordRepository paymentCallbackRecordRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final OrderCommandFacade orderCommandFacade;

    public PaymentCallbackApplicationService(PaymentOrderRepository paymentOrderRepository,
                                             PaymentCallbackRecordRepository paymentCallbackRecordRepository,
                                             PaymentAuditLogRepository paymentAuditLogRepository,
                                             OrderCommandFacade orderCommandFacade) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentCallbackRecordRepository = paymentCallbackRecordRepository;
        this.paymentAuditLogRepository = paymentAuditLogRepository;
        this.orderCommandFacade = orderCommandFacade;
    }

    public void callbackPaid(String channelCode, Long tenantId, String paymentNo, String channelTransactionNo,
                             String channelStatus, String rawPayload) {
        validateChannel(channelCode);
        if (channelTransactionNo == null || channelTransactionNo.isBlank()) {
            throw new IllegalArgumentException("Channel transaction no must not be blank");
        }
        if (channelStatus == null || channelStatus.isBlank()) {
            throw new IllegalArgumentException("Channel status must not be blank");
        }
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Raw payload must not be blank");
        }
        PaymentOrder paymentOrder = paymentOrderRepository.findOrderByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
        PaymentCallbackRecord existing = paymentCallbackRecordRepository
                .findCallbackByChannelTransactionNo(tenantId, channelCode, channelTransactionNo)
                .orElse(null);
        PaymentCallbackRecord callbackRecord = existing == null
                ? paymentCallbackRecordRepository.save(new PaymentCallbackRecord(null, tenantId,
                paymentNo, paymentOrder.getOrderNo(), channelCode, channelTransactionNo, channelStatus, rawPayload, Instant.now()))
                : existing;
        if (PaymentOrder.STATUS_PAID.equals(paymentOrder.getPaymentStatus())) {
            return;
        }
        if (PaymentOrder.STATUS_FAILED.equals(paymentOrder.getPaymentStatus())
                || PaymentOrder.STATUS_CLOSED.equals(paymentOrder.getPaymentStatus())) {
            return;
        }
        String beforeStatus = paymentOrder.getPaymentStatus();
        Instant paidTime = Instant.now();
        paymentOrder.markPaid(paymentOrder.getAmount(), paidTime, callbackRecord.getChannelTransactionNo(),
                callbackRecord.getChannelStatus(), callbackRecord.summarize());
        paymentOrderRepository.save(paymentOrder);
        paymentAuditLogRepository.save(new PaymentAuditLog(null, tenantId, paymentNo,
                PaymentAuditLog.ACTION_CALLBACK_PAID, beforeStatus, paymentOrder.getPaymentStatus(),
                PaymentAuditLog.OPERATOR_CHANNEL, 0L, paidTime));
        orderCommandFacade.markPaid(tenantId, paymentOrder.getOrderNo(), paymentNo, channelCode,
                paymentOrder.getAmount(), paidTime);
    }

    public void callbackFailed(String channelCode, Long tenantId, String paymentNo, String channelStatus,
                               String rawPayload, String reason) {
        validateChannel(channelCode);
        if (channelStatus == null || channelStatus.isBlank()) {
            throw new IllegalArgumentException("Channel status must not be blank");
        }
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Raw payload must not be blank");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Failure reason must not be blank");
        }
        PaymentOrder paymentOrder = paymentOrderRepository.findOrderByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
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
            return;
        }
        if (PaymentOrder.STATUS_FAILED.equals(paymentOrder.getPaymentStatus())
                || PaymentOrder.STATUS_CLOSED.equals(paymentOrder.getPaymentStatus())) {
            return;
        }
        String beforeStatus = paymentOrder.getPaymentStatus();
        Instant failedTime = Instant.now();
        paymentOrder.markFailed(channelStatus, rawPayload.length() <= 255 ? rawPayload : rawPayload.substring(0, 255));
        paymentOrderRepository.save(paymentOrder);
        paymentAuditLogRepository.save(new PaymentAuditLog(null, tenantId, paymentNo,
                PaymentAuditLog.ACTION_CALLBACK_FAILED, beforeStatus, paymentOrder.getPaymentStatus(),
                PaymentAuditLog.OPERATOR_CHANNEL, 0L, failedTime));
        orderCommandFacade.markPaymentFailed(tenantId, paymentOrder.getOrderNo(), paymentNo, reason, channelStatus, failedTime);
    }

    private void validateChannel(String channelCode) {
        if (!PaymentOrder.CHANNEL_MOCK.equals(channelCode)) {
            throw new IllegalArgumentException("Unsupported channel code: " + channelCode);
        }
    }
}
