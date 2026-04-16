package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.application.result.OrderPageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 订单分页响应对象。
 */
@Schema(description = "订单分页响应")
public record OrderPageResponse(
        /** 当前页记录。 */
        @Schema(description = "分页记录") List<OrderSummaryResponse> records,
        /** 总记录数。 */
        @Schema(description = "总条数", example = "20") long total,
        /** 页码。 */
        @Schema(description = "页码", example = "1") int pageNo,
        /** 每页条数。 */
        @Schema(description = "页大小", example = "20") int pageSize) {

    public static OrderPageResponse from(OrderPageResult dto) {
        List<OrderSummaryResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(OrderSummaryResponse::from).toList();
        return new OrderPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
