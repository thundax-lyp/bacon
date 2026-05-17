package com.github.thundax.bacon.product.api.facade;

import com.github.thundax.bacon.product.api.request.ProductOrderSnapshotCreateFacadeRequest;
import com.github.thundax.bacon.product.api.response.ProductOrderSnapshotCreateFacadeResponse;

public interface ProductCommandFacade {

    ProductOrderSnapshotCreateFacadeResponse createOrderProductSnapshot(
            ProductOrderSnapshotCreateFacadeRequest request);
}
