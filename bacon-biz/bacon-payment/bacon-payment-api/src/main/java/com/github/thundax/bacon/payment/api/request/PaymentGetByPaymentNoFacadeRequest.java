package com.github.thundax.bacon.payment.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGetByPaymentNoFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String paymentNo;
}
