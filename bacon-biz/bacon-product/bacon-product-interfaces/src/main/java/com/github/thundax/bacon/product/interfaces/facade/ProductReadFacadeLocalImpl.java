package com.github.thundax.bacon.product.interfaces.facade;

import com.github.thundax.bacon.product.api.dto.ProductSkuSaleInfoDTO;
import com.github.thundax.bacon.product.api.facade.ProductReadFacade;
import com.github.thundax.bacon.product.application.service.ProductSearchApplicationService;
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
    public ProductSkuSaleInfoDTO getSkuSaleInfo(Long tenantId, Long skuId) {
        return ProductInterfaceAssembler.toSkuSaleInfoDTO(productSearchApplicationService.getSkuSaleInfo(skuId));
    }
}
