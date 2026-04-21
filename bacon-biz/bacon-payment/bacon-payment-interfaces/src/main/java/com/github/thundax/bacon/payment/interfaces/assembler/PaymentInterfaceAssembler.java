package com.github.thundax.bacon.payment.interfaces.assembler;

import com.github.thundax.bacon.payment.application.audit.PaymentAuditLogQuery;
import com.github.thundax.bacon.payment.application.command.PaymentCallbackFailedCommand;
import com.github.thundax.bacon.payment.application.command.PaymentCallbackPaidCommand;
import com.github.thundax.bacon.payment.application.command.PaymentCloseCommand;
import com.github.thundax.bacon.payment.application.command.PaymentCloseResult;
import com.github.thundax.bacon.payment.application.command.PaymentCreateCommand;
import com.github.thundax.bacon.payment.application.command.PaymentCreateResult;
import com.github.thundax.bacon.payment.application.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.query.PaymentGetByOrderNoQuery;
import com.github.thundax.bacon.payment.application.query.PaymentGetByPaymentNoQuery;
import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;
import com.github.thundax.bacon.payment.interfaces.request.PaymentCallbackRequest;
import com.github.thundax.bacon.payment.interfaces.response.PaymentAuditLogResponse;
import com.github.thundax.bacon.payment.interfaces.response.PaymentCloseResponse;
import com.github.thundax.bacon.payment.interfaces.response.PaymentCreateResponse;
import com.github.thundax.bacon.payment.interfaces.response.PaymentDetailResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PaymentInterfaceAssembler {

    private PaymentInterfaceAssembler() {}

    public static PaymentCreateCommand toCreateCommand(PaymentCreateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return toCreateCommand(
                request.getOrderNo(),
                request.getUserId(),
                request.getAmount(),
                request.getChannelCode(),
                request.getSubject(),
                request.getExpiredAt());
    }

    public static PaymentCreateCommand toCreateCommand(
            String orderNo, Long userId, BigDecimal amount, String channelCode, String subject, Instant expiredAt) {
        return new PaymentCreateCommand(orderNo, userId, amount, channelCode, subject, expiredAt);
    }

    public static PaymentCloseCommand toCloseCommand(PaymentCloseFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return toCloseCommand(request.getPaymentNo(), request.getReason());
    }

    public static PaymentCloseCommand toCloseCommand(String paymentNo, String reason) {
        return new PaymentCloseCommand(paymentNo, reason);
    }

    public static PaymentGetByPaymentNoQuery toGetByPaymentNoQuery(PaymentGetByPaymentNoFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return toGetByPaymentNoQuery(request.getPaymentNo());
    }

    public static PaymentGetByPaymentNoQuery toGetByPaymentNoQuery(String paymentNo) {
        return new PaymentGetByPaymentNoQuery(paymentNo);
    }

    public static PaymentGetByOrderNoQuery toGetByOrderNoQuery(PaymentGetByOrderNoFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return toGetByOrderNoQuery(request.getOrderNo());
    }

    public static PaymentGetByOrderNoQuery toGetByOrderNoQuery(String orderNo) {
        return new PaymentGetByOrderNoQuery(orderNo);
    }

    public static PaymentAuditLogQuery toAuditLogQuery(String paymentNo) {
        return paymentNo == null ? null : new PaymentAuditLogQuery(paymentNo);
    }

    public static PaymentCallbackPaidCommand toCallbackPaidCommand(
            String channelCode, PaymentCallbackRequest request) {
        if (request == null) {
            return null;
        }
        return new PaymentCallbackPaidCommand(
                channelCode, request.getPaymentNo(), request.getChannelTransactionNo(), request.getChannelStatus(),
                request.getRawPayload());
    }

    public static PaymentCallbackFailedCommand toCallbackFailedCommand(
            String channelCode, PaymentCallbackRequest request) {
        if (request == null) {
            return null;
        }
        return new PaymentCallbackFailedCommand(
                channelCode, request.getPaymentNo(), request.getChannelStatus(), request.getRawPayload(),
                request.getReason());
    }

    public static PaymentCreateResponse toCreateResponse(PaymentCreateResult dto) {
        if (dto == null) {
            return null;
        }
        return new PaymentCreateResponse(
                dto.getPaymentNo(),
                dto.getOrderNo(),
                dto.getChannelCode(),
                dto.getPaymentStatus(),
                dto.getPayPayload(),
                dto.getExpiredAt(),
                dto.getFailureReason());
    }

    public static PaymentCloseResponse toCloseResponse(PaymentCloseResult dto) {
        if (dto == null) {
            return null;
        }
        return new PaymentCloseResponse(
                dto.getPaymentNo(),
                dto.getOrderNo(),
                dto.getPaymentStatus(),
                dto.getCloseResult(),
                dto.getCloseReason(),
                dto.getFailureReason());
    }

    public static PaymentDetailResponse toDetailResponse(PaymentDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        return new PaymentDetailResponse(
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

    public static List<PaymentAuditLogResponse> toAuditLogResponses(List<PaymentAuditLogDTO> auditLogs) {
        return auditLogs.stream()
                .map(dto -> new PaymentAuditLogResponse(
                        dto.paymentNo(),
                        dto.actionType(),
                        dto.beforeStatus(),
                        dto.afterStatus(),
                        dto.operatorType(),
                        dto.operatorId(),
                        dto.occurredAt()))
                .toList();
    }

    public static PaymentCreateFacadeResponse toCreateFacadeResponse(PaymentCreateResult dto) {
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

    public static PaymentCloseFacadeResponse toCloseFacadeResponse(PaymentCloseResult dto) {
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

    public static PaymentDetailFacadeResponse toDetailFacadeResponse(PaymentDetailDTO dto) {
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
