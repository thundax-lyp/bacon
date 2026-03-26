package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.application.query.GetOrderQuery;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryApplicationService {

    private final OrderApplicationService orderApplicationService;

    public OrderQueryApplicationService(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    public OrderDetailDTO getById(Long tenantId, Long orderId) {
        OrderDetailDTO dto = orderApplicationService.get(new GetOrderQuery(orderId));
        if (tenantId != null && !tenantId.equals(dto.getTenantId())) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return dto;
    }

    public OrderDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return orderApplicationService.getByOrderNo(tenantId, orderNo);
    }

    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        return orderApplicationService.pageOrders(query);
    }
}
