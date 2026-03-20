package com.github.thundax.bacon.order.api.facade;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;

public interface OrderReadFacade {

    OrderSummaryDTO getById(Long orderId);
}
