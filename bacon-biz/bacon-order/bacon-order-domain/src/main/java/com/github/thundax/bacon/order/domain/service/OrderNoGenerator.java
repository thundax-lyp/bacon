package com.github.thundax.bacon.order.domain.service;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;

public interface OrderNoGenerator {

    OrderNo nextOrderNo();
}
