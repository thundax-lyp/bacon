package com.github.thundax.bacon.order.domain.service;

import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;

public interface OrderNoGenerator {

    OrderNo nextOrderNo();
}
