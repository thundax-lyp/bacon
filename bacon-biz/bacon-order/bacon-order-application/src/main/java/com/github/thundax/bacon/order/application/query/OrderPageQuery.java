package com.github.thundax.bacon.order.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import java.time.Instant;
import lombok.Getter;

@Getter
public class OrderPageQuery extends PageQuery {

    private final UserId userId;
    private final OrderNo orderNo;
    private final OrderStatus orderStatus;
    private final PayStatus payStatus;
    private final InventoryStatus inventoryStatus;
    private final Instant createdAtFrom;
    private final Instant createdAtTo;

    public OrderPageQuery(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            Integer pageNo,
            Integer pageSize) {
        super(pageNo, pageSize);
        this.userId = userId;
        this.orderNo = orderNo;
        this.orderStatus = orderStatus;
        this.payStatus = payStatus;
        this.inventoryStatus = inventoryStatus;
        this.createdAtFrom = createdAtFrom;
        this.createdAtTo = createdAtTo;
    }
}
