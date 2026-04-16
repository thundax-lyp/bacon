package com.github.thundax.bacon.order.api.query;

import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单分页查询条件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageQuery {

    /** 下单用户主键。 */
    private Long userId;
    /** 订单号。 */
    @Size(max = 64)
    private String orderNo;
    /** 订单状态。 */
    @Size(max = 32)
    private String orderStatus;
    /** 支付状态。 */
    @Size(max = 16)
    private String payStatus;
    /** 库存状态。 */
    @Size(max = 16)
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
