package com.github.thundax.bacon.product.api.facade;

import com.github.thundax.bacon.product.api.dto.ProductSkuSaleInfoDTO;

public interface ProductReadFacade {

    ProductSkuSaleInfoDTO getSkuSaleInfo(Long tenantId, Long skuId);
}
