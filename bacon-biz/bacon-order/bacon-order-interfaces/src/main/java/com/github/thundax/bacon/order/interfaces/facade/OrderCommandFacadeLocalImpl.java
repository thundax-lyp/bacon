package com.github.thundax.bacon.order.interfaces.facade;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class OrderCommandFacadeLocalImpl implements OrderCommandFacade {

    private final OrderPaymentResultApplicationService orderPaymentResultApplicationService;
    private final OrderTimeoutApplicationService orderTimeoutApplicationService;

    public OrderCommandFacadeLocalImpl(
            OrderPaymentResultApplicationService orderPaymentResultApplicationService,
            OrderTimeoutApplicationService orderTimeoutApplicationService) {
        this.orderPaymentResultApplicationService = orderPaymentResultApplicationService;
        this.orderTimeoutApplicationService = orderTimeoutApplicationService;
    }

    @Override
    public void markPaid(
            String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        BaconContextHolder.requireTenantId();
        orderPaymentResultApplicationService.markPaid(
                OrderNoCodec.toDomain(orderNo), PaymentNoCodec.toDomain(paymentNo), channelCode, paidAmount, paidTime);
    }

    @Override
    public void markPaymentFailed(
            String orderNo, String paymentNo, String reason, String channelStatus, Instant failedTime) {
        BaconContextHolder.requireTenantId();
        orderPaymentResultApplicationService.markPaymentFailed(
                OrderNoCodec.toDomain(orderNo), PaymentNoCodec.toDomain(paymentNo), reason, channelStatus, failedTime);
    }

    @Override
    public void closeExpiredOrder(String orderNo, String reason) {
        BaconContextHolder.requireTenantId();
        orderTimeoutApplicationService.closeExpiredOrder(OrderNoCodec.toDomain(orderNo), reason);
    }
}
