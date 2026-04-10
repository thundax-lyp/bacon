package com.github.thundax.bacon.order.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
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
    public OrderDetailDTO getById(Long orderId) {
        Long tenantId = requireTenantId();
        return orderQueryService.getById(tenantId, orderId);
    }

    @Override
    public OrderDetailDTO getByOrderNo(String orderNo) {
        Long tenantId = requireTenantId();
        return orderQueryService.getByOrderNo(tenantId, orderNo);
    }

    @Override
    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        return orderQueryService.pageOrders(query);
    }

    private Long requireTenantId() {
        Long tenantId = BaconContextHolder.currentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("tenantId must not be null");
        }
        return tenantId;
    }
}
