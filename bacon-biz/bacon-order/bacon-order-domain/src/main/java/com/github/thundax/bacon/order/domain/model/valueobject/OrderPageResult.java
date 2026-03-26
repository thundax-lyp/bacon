package com.github.thundax.bacon.order.domain.model.valueobject;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import java.util.List;

public record OrderPageResult(
        List<Order> records,
        long total
) {
}
