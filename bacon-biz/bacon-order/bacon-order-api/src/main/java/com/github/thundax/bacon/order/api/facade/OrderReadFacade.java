package com.github.thundax.bacon.order.api.facade;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.query.OrderPageQuery;

public interface OrderReadFacade {

    OrderDetailDTO getById(Long orderId);

    OrderDetailDTO getByOrderNo(String orderNo);

    OrderPageResultDTO pageOrders(OrderPageQuery query);
}
