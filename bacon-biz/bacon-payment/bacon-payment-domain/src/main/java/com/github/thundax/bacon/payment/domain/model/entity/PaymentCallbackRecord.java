package com.github.thundax.bacon.payment.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PaymentCallbackRecord {

    private final Long id;
    private final Long tenantId;
    private final String paymentNo;
    private final String orderNo;
    private final String channelCode;
    private final String channelTransactionNo;
    private final String channelStatus;
    private final String rawPayload;
    private final Instant receivedAt;

    public PaymentCallbackRecord(Long id, Long tenantId, String paymentNo, String orderNo, String channelCode,
                                 String channelTransactionNo, String channelStatus, String rawPayload, Instant receivedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.channelCode = channelCode;
        this.channelTransactionNo = channelTransactionNo;
        this.channelStatus = channelStatus;
        this.rawPayload = rawPayload;
        this.receivedAt = receivedAt;
    }

    public String summarize() {
        if (rawPayload == null || rawPayload.isBlank()) {
            return channelStatus;
        }
        return rawPayload.length() <= 255 ? rawPayload : rawPayload.substring(0, 255);
    }
}
