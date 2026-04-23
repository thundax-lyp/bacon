package com.github.thundax.bacon.payment.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCloseFacadeResponse {

    private String paymentNo;
    private String orderNo;
    private String paymentStatus;
    private String closeResult;
    private String closeReason;
    private String failureReason;
}
