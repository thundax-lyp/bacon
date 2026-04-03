package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 订单分页查询条件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageQueryDTO {

    /** 所属租户主键。 */
    private String tenantId;
    /** 下单用户主键。 */
    private String userId;
    /** 订单号。 */
    private String orderNo;
    /** 订单状态。 */
    private String orderStatus;
    /** 支付状态。 */
    private String payStatus;
    /** 库存状态。 */
    private String inventoryStatus;
    /** 创建开始时间。 */
    private Instant createdAtFrom;
    /** 创建结束时间。 */
    private Instant createdAtTo;
    /** 页码。 */
    private Integer pageNo;
    /** 每页条数。 */
    private Integer pageSize;
}
