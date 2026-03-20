package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.application.query.GetOrderQuery;
import com.github.thundax.bacon.order.application.service.OrderApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono")
public class OrderReadFacadeLocalImpl implements OrderReadFacade {

    private final OrderApplicationService orderApplicationService;

    public OrderReadFacadeLocalImpl(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @Override
    public OrderSummaryDTO getById(Long orderId) {
        return orderApplicationService.get(new GetOrderQuery(orderId));
    }
}
