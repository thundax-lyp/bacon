package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "订单分页响应")
public record OrderPageResponse(
        @Schema(description = "分页记录") List<OrderSummaryResponse> records,
        @Schema(description = "总条数", example = "20") long total,
        @Schema(description = "页码", example = "1") int pageNo,
        @Schema(description = "页大小", example = "20") int pageSize) {

    public static OrderPageResponse from(OrderPageResultDTO dto) {
        List<OrderSummaryResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(OrderSummaryResponse::from).toList();
        return new OrderPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
