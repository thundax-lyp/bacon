package com.github.thundax.bacon.payment.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGetByPaymentNoFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String paymentNo;
}
