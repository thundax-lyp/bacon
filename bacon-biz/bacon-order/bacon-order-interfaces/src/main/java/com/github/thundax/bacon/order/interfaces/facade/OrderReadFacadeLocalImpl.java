package com.github.thundax.bacon.order.interfaces.facade;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.application.service.OrderQueryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class OrderReadFacadeLocalImpl implements OrderReadFacade {

    private final OrderQueryService orderQueryService;

    public OrderReadFacadeLocalImpl(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @Override
    public OrderDetailDTO getById(Long tenantId, Long orderId) {
        return orderQueryService.getById(tenantId, orderId);
    }

    @Override
    public OrderDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return orderQueryService.getByOrderNo(tenantId, orderNo);
    }

    @Override
    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        return orderQueryService.pageOrders(query);
    }
}
