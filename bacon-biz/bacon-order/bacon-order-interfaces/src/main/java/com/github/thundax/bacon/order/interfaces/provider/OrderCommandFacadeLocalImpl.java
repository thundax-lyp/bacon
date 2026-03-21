package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.application.service.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.service.OrderTimeoutApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class OrderCommandFacadeLocalImpl implements OrderCommandFacade {

    private final OrderPaymentResultApplicationService orderPaymentResultApplicationService;
    private final OrderTimeoutApplicationService orderTimeoutApplicationService;

    public OrderCommandFacadeLocalImpl(OrderPaymentResultApplicationService orderPaymentResultApplicationService,
                                       OrderTimeoutApplicationService orderTimeoutApplicationService) {
        this.orderPaymentResultApplicationService = orderPaymentResultApplicationService;
        this.orderTimeoutApplicationService = orderTimeoutApplicationService;
    }

    @Override
    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount,
                         Instant paidTime) {
        orderPaymentResultApplicationService.markPaid(tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime);
    }

    @Override
    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                  Instant failedTime) {
        orderPaymentResultApplicationService.markPaymentFailed(tenantId, orderNo, paymentNo, reason, channelStatus, failedTime);
    }

    @Override
    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        orderTimeoutApplicationService.closeExpiredOrder(tenantId, orderNo, reason);
    }
}
