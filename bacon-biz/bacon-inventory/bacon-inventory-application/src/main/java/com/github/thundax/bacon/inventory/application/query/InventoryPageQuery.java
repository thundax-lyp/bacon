package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;

public class InventoryPageQuery extends PageQuery {

    private final SkuId skuId;
    private final InventoryStatus status;

    public InventoryPageQuery(SkuId skuId, InventoryStatus status, Integer pageNo, Integer pageSize) {
        super(pageNo, pageSize);
        this.skuId = skuId;
        this.status = status;
    }

    public SkuId getSkuId() {
        return skuId;
    }

    public InventoryStatus getStatus() {
        return status;
    }
}
