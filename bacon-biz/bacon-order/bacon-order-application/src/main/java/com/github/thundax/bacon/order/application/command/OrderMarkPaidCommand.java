package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderMarkPaidCommand(
        OrderNo orderNo, PaymentNo paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {}
