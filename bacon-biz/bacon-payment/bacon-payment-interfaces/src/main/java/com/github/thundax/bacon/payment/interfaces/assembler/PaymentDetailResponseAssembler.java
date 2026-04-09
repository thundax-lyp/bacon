package com.github.thundax.bacon.payment.interfaces.assembler;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.interfaces.response.PaymentDetailResponse;

public final class PaymentDetailResponseAssembler {

    private PaymentDetailResponseAssembler() {}

    public static PaymentDetailResponse from(PaymentDetailDTO dto) {
        return new PaymentDetailResponse(
                dto.getTenantId(),
                dto.getPaymentNo(),
                dto.getOrderNo(),
                dto.getUserId(),
                dto.getChannelCode(),
                dto.getPaymentStatus(),
                dto.getAmount(),
                dto.getPaidAmount(),
                dto.getCreatedAt(),
                dto.getExpiredAt(),
                dto.getPaidAt(),
                dto.getSubject(),
                dto.getClosedAt(),
                dto.getChannelTransactionNo(),
                dto.getChannelStatus(),
                dto.getCallbackSummary());
    }
}
