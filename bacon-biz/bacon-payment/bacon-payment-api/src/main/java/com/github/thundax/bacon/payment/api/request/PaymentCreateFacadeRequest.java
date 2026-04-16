package com.github.thundax.bacon.payment.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    @Size(max = 32)
    private String channelCode;

    @NotBlank
    @Size(max = 255)
    private String subject;

    @NotNull
    private Instant expiredAt;
}
