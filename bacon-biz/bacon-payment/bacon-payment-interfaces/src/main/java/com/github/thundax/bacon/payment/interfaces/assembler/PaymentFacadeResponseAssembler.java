package com.github.thundax.bacon.payment.interfaces.assembler;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;

public final class PaymentFacadeResponseAssembler {

    private PaymentFacadeResponseAssembler() {}

    public static PaymentCreateFacadeResponse fromCreateResult(PaymentCreateResultDTO dto) {
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

    public static PaymentCloseFacadeResponse fromCloseResult(PaymentCloseResultDTO dto) {
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
