package com.github.thundax.bacon.order.interfaces.facade;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
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
    public void markPaid(OrderMarkPaidFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        orderPaymentResultApplicationService.markPaid(
                OrderNoCodec.toDomain(request.getOrderNo()),
                PaymentNoCodec.toDomain(request.getPaymentNo()),
                request.getChannelCode(),
                request.getPaidAmount(),
                request.getPaidTime());
    }

    @Override
    public void markPaymentFailed(OrderMarkPaymentFailedFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        orderPaymentResultApplicationService.markPaymentFailed(
                OrderNoCodec.toDomain(request.getOrderNo()),
                PaymentNoCodec.toDomain(request.getPaymentNo()),
                request.getReason(),
                request.getChannelStatus(),
                request.getFailedTime());
    }

    @Override
    public void closeExpiredOrder(OrderCloseExpiredFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        orderTimeoutApplicationService.closeExpiredOrder(OrderNoCodec.toDomain(request.getOrderNo()), request.getReason());
    }
}
