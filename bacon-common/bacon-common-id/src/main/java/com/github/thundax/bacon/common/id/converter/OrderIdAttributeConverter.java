package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.domain.OrderId;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OrderIdAttributeConverter extends AbstractIdAttributeConverter<OrderId, String> {

    public OrderIdAttributeConverter() {
        super(OrderId::of, OrderId::value);
    }
}
