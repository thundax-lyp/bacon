package com.github.thundax.bacon.order.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.interfaces.assembler.OrderInterfaceAssembler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class OrderReadFacadeLocalImpl implements OrderReadFacade {

    private final OrderQueryApplicationService orderQueryService;

    public OrderReadFacadeLocalImpl(OrderQueryApplicationService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @Override
    public OrderDetailFacadeResponse getByOrderNo(OrderDetailFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return OrderInterfaceAssembler.toDetailFacadeResponse(
                orderQueryService.getByOrderNo(OrderInterfaceAssembler.toByOrderNoQuery(request.getOrderNo())));
    }

    @Override
    public OrderPageFacadeResponse page(OrderPageFacadeRequest request) {
        return OrderInterfaceAssembler.toPageFacadeResponse(
                orderQueryService.page(OrderInterfaceAssembler.toPageQuery(request)));
    }
}
