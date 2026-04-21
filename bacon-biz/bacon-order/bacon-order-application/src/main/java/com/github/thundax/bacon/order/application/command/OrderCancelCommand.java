package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;

public record OrderCancelCommand(OrderNo orderNo, String reason) {}
