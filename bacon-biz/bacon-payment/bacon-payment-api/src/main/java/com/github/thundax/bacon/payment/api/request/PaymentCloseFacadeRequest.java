package com.github.thundax.bacon.payment.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCloseFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String paymentNo;

    @NotBlank
    @Size(max = 32)
    private String reason;
}
