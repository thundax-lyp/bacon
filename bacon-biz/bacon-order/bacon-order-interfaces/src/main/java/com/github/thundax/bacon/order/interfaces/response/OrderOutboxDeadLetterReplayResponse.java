package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.application.result.OrderOutboxDeadLetterReplayResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单出站死信回放结果")
public record OrderOutboxDeadLetterReplayResponse(
        @Schema(description = "死信ID", example = "1001") Long deadLetterId,
        @Schema(description = "回放状态", example = "SUCCESS") String replayStatus,
        @Schema(description = "结果说明", example = "ok") String message) {

    public static OrderOutboxDeadLetterReplayResponse from(OrderOutboxDeadLetterReplayResult result) {
        return new OrderOutboxDeadLetterReplayResponse(
                result.deadLetterId(),
                result.replayStatus(),
                result.message());
    }
}
