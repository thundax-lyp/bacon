package com.github.thundax.bacon.payment.api.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateFacadeResponse {

    private String paymentNo;
    private String orderNo;
    private String channelCode;
    private String paymentStatus;
    private String payPayload;
    private Instant expiredAt;
    private String failureReason;
}
