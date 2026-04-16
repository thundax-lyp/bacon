package com.github.thundax.bacon.order.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单分页门面响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageFacadeResponse {

    private List<OrderSummaryFacadeResponse> records;
    private long total;
    private int pageNo;
    private int pageSize;
}
