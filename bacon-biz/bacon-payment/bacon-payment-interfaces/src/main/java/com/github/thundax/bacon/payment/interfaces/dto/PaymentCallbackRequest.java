package com.github.thundax.bacon.payment.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {

    private Long tenantId;
    private String paymentNo;
    private String result;
    private String reason;
    private String channelTransactionNo;
}
