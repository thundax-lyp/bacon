package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;

public class InventoryAuditDeadLetterPageQuery extends PageQuery {

    private final OrderNo orderNo;
    private final InventoryAuditReplayStatus replayStatus;

    public InventoryAuditDeadLetterPageQuery(
            OrderNo orderNo, InventoryAuditReplayStatus replayStatus, Integer pageNo, Integer pageSize) {
        super(pageNo, pageSize);
        this.orderNo = orderNo;
        this.replayStatus = replayStatus;
    }

    public OrderNo getOrderNo() {
        return orderNo;
    }

    public InventoryAuditReplayStatus getReplayStatus() {
        return replayStatus;
    }
}
