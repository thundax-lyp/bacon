package com.github.thundax.bacon.product.interfaces.facade;

import com.github.thundax.bacon.product.api.facade.ProductReadFacade;
import com.github.thundax.bacon.product.api.request.ProductSkuSaleInfoFacadeRequest;
import com.github.thundax.bacon.product.api.response.ProductSkuSaleInfoFacadeResponse;
import com.github.thundax.bacon.product.application.query.ProductSearchApplicationService;
import com.github.thundax.bacon.product.interfaces.assembler.ProductInterfaceAssembler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class ProductReadFacadeLocalImpl implements ProductReadFacade {

    private final ProductSearchApplicationService productSearchApplicationService;

    public ProductReadFacadeLocalImpl(ProductSearchApplicationService productSearchApplicationService) {
        this.productSearchApplicationService = productSearchApplicationService;
    }

    @Override
    public ProductSkuSaleInfoFacadeResponse getSkuSaleInfo(ProductSkuSaleInfoFacadeRequest request) {
        return new ProductSkuSaleInfoFacadeResponse(
                ProductInterfaceAssembler.toSkuSaleInfoDTO(productSearchApplicationService.getSkuSaleInfo(request.skuId())));
    }
}
