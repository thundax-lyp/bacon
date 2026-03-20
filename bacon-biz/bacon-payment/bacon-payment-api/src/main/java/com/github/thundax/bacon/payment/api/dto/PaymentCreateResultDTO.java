package com.github.thundax.bacon.payment.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResultDTO {

    private Long tenantId;
    private String paymentNo;
    private String orderNo;
    private String channelCode;
    private String paymentStatus;
    private String payPayload;
    private Instant expiredAt;
    private String failureReason;
}
