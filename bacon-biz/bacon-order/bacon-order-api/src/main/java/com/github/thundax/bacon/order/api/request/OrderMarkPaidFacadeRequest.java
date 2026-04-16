package com.github.thundax.bacon.order.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付成功回写门面请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMarkPaidFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;

    @NotBlank
    @Size(max = 64)
    private String paymentNo;

    @NotBlank
    @Size(max = 32)
    private String channelCode;

    @NotNull
    private BigDecimal paidAmount;

    @NotNull
    private Instant paidTime;
}
