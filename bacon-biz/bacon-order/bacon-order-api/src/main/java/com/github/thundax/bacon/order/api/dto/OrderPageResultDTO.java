package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单分页结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageResultDTO {

    /** 当前页记录。 */
    private List<OrderSummaryDTO> records;
    /** 总记录数。 */
    private long total;
    /** 页码。 */
    private int pageNo;
    /** 每页条数。 */
    private int pageSize;
}
