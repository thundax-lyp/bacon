package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;

public record OrderCloseExpiredCommand(OrderNo orderNo, String reason) {}
