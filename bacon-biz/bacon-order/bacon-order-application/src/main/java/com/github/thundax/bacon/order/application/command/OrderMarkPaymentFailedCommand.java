package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import java.time.Instant;

public record OrderMarkPaymentFailedCommand(
        OrderNo orderNo, PaymentNo paymentNo, String reason, String channelStatus, Instant failedTime) {}
