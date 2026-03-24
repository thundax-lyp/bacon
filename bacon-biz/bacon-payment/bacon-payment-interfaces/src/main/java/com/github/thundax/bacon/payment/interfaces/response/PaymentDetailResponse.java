package com.github.thundax.bacon.payment.interfaces.response;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDetailResponse(Long tenantId, String paymentNo, String orderNo, Long userId, String channelCode,
                                    String paymentStatus, BigDecimal amount, BigDecimal paidAmount,
                                    Instant createdAt, Instant expiredAt, Instant paidAt, String subject,
                                    Instant closedAt, String channelTransactionNo, String channelStatus,
                                    String callbackSummary) {

    public static PaymentDetailResponse from(PaymentDetailDTO dto) {
        return new PaymentDetailResponse(dto.getTenantId(), dto.getPaymentNo(), dto.getOrderNo(), dto.getUserId(),
                dto.getChannelCode(), dto.getPaymentStatus(), dto.getAmount(), dto.getPaidAmount(),
                dto.getCreatedAt(), dto.getExpiredAt(), dto.getPaidAt(), dto.getSubject(), dto.getClosedAt(),
                dto.getChannelTransactionNo(), dto.getChannelStatus(), dto.getCallbackSummary());
    }
}
