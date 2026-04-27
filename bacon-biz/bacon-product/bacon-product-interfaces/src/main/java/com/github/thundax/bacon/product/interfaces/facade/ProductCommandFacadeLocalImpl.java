package com.github.thundax.bacon.product.interfaces.facade;

import com.github.thundax.bacon.product.api.dto.ProductSnapshotDTO;
import com.github.thundax.bacon.product.api.facade.ProductCommandFacade;
import com.github.thundax.bacon.product.application.command.CreateOrderProductSnapshotCommand;
import com.github.thundax.bacon.product.application.service.ProductSnapshotApplicationService;
import com.github.thundax.bacon.product.interfaces.assembler.ProductInterfaceAssembler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class ProductCommandFacadeLocalImpl implements ProductCommandFacade {

    private final ProductSnapshotApplicationService productSnapshotApplicationService;

    public ProductCommandFacadeLocalImpl(ProductSnapshotApplicationService productSnapshotApplicationService) {
        this.productSnapshotApplicationService = productSnapshotApplicationService;
    }

    @Override
    public ProductSnapshotDTO createOrderProductSnapshot(
            Long tenantId, String orderNo, String orderItemNo, Long skuId, Integer quantity) {
        return ProductInterfaceAssembler.toSnapshotDTO(
                productSnapshotApplicationService.createOrderProductSnapshot(
                        new CreateOrderProductSnapshotCommand(tenantId, orderNo, orderItemNo, skuId, quantity)));
    }
}
