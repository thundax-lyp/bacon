package com.github.thundax.bacon.order.infra.persistence.mapper;

import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderDataObject;

public interface OrderMapper {

    OrderDataObject selectById(Long id);
}
