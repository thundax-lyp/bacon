package com.github.thundax.bacon.payment.interfaces.assembler;

import com.github.thundax.bacon.payment.application.command.PaymentCloseResult;
import com.github.thundax.bacon.payment.application.command.PaymentCreateResult;
import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;

public final class PaymentFacadeResponseAssembler {

    private PaymentFacadeResponseAssembler() {}

    public static PaymentCreateFacadeResponse fromCreateResult(PaymentCreateResult dto) {
        if (dto == null) {
            return null;
        }
        return new PaymentCreateFacadeResponse(
                dto.getPaymentNo(),
                dto.getOrderNo(),
                dto.getChannelCode(),
                dto.getPaymentStatus(),
                dto.getPayPayload(),
                dto.getExpiredAt(),
                dto.getFailureReason());
    }

    public static PaymentCloseFacadeResponse fromCloseResult(PaymentCloseResult dto) {
        if (dto == null) {
            return null;
        }
        return new PaymentCloseFacadeResponse(
                dto.getPaymentNo(),
                dto.getOrderNo(),
                dto.getPaymentStatus(),
                dto.getCloseResult(),
                dto.getCloseReason(),
                dto.getFailureReason());
    }

    public static PaymentDetailFacadeResponse fromDetail(PaymentDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        return new PaymentDetailFacadeResponse(
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
