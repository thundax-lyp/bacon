package com.github.thundax.bacon.order.api.request;

import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单分页门面请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageFacadeRequest {

    private Long userId;

    @Size(max = 64)
    private String orderNo;

    @Size(max = 32)
    private String orderStatus;

    @Size(max = 16)
    private String payStatus;

    @Size(max = 16)
    private String inventoryStatus;

    private Instant createdAtFrom;

    private Instant createdAtTo;

    private Integer pageNo;

    private Integer pageSize;
}
