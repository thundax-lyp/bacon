package com.github.thundax.bacon.product.api.facade;

import com.github.thundax.bacon.product.api.request.ProductSkuSaleInfoFacadeRequest;
import com.github.thundax.bacon.product.api.response.ProductSkuSaleInfoFacadeResponse;

public interface ProductReadFacade {

    ProductSkuSaleInfoFacadeResponse getSkuSaleInfo(ProductSkuSaleInfoFacadeRequest request);
}
