package com.github.thundax.bacon.order.api.facade;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;

public interface OrderReadFacade {

    OrderDetailDTO getById(Long tenantId, Long orderId);

    OrderDetailDTO getByOrderNo(Long tenantId, String orderNo);

    OrderPageResultDTO pageOrders(OrderPageQueryDTO query);
}
