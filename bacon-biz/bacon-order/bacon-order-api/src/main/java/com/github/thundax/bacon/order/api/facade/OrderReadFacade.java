package com.github.thundax.bacon.order.api.facade;

import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;

public interface OrderReadFacade {

    OrderDetailFacadeResponse getByOrderNo(OrderDetailFacadeRequest request);

    OrderPageFacadeResponse pageOrders(OrderPageFacadeRequest request);
}
