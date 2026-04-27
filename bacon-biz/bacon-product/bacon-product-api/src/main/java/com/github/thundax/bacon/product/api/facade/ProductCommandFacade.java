package com.github.thundax.bacon.product.api.facade;

import com.github.thundax.bacon.product.api.dto.ProductSnapshotDTO;

public interface ProductCommandFacade {

    ProductSnapshotDTO createOrderProductSnapshot(
            Long tenantId, String orderNo, String orderItemNo, Long skuId, Integer quantity);
}
