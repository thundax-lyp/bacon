package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageQueryDTO {

    private Long tenantId;
    private Long userId;
    private String orderNo;
    private String orderStatus;
    private String payStatus;
    private String inventoryStatus;
    private Instant createdAtFrom;
    private Instant createdAtTo;
    private Integer pageNo;
    private Integer pageSize;
}
