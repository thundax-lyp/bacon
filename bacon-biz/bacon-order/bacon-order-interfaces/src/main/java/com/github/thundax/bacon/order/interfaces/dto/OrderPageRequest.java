package com.github.thundax.bacon.order.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageRequest {

    @Positive
    private Long userId;

    @Size(max = 64)
    private String orderNo;

    @Schema(
            description = "订单状态",
            allowableValues = {"CREATED", "PAID", "CANCELLED", "CLOSED", "COMPLETED"},
            example = "CREATED")
    @Size(max = 32)
    private String orderStatus;

    @Schema(
            description = "支付状态",
            allowableValues = {"UNPAID", "PAYING", "PAID", "PAY_FAILED", "REFUNDED"},
            example = "UNPAID")
    @Size(max = 16)
    private String payStatus;

    @Schema(
            description = "库存状态",
            allowableValues = {"UNRESERVED", "RESERVING", "RESERVED", "RESERVE_FAILED", "RELEASED"},
            example = "UNRESERVED")
    @Size(max = 16)
    private String inventoryStatus;

    private Instant createdAtFrom;

    private Instant createdAtTo;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
