package com.github.thundax.bacon.order.application.command;

import java.time.Instant;
import java.util.List;

public record CreateOrderCommand(
        Long userId,
        String currencyCode,
        String channelCode,
        String remark,
        Instant expiredAt,
        List<CreateOrderItemCommand> items) {}
