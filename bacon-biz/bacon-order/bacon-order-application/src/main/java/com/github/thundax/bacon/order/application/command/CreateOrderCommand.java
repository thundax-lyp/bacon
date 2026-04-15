package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;
import java.time.Instant;
import java.util.List;

public record CreateOrderCommand(
        UserId userId,
        String currencyCode,
        String channelCode,
        String remark,
        Instant expiredAt,
        List<CreateOrderItemCommand> items) {}
