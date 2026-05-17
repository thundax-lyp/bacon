package com.github.thundax.bacon.product.interfaces.facade;

import com.github.thundax.bacon.product.api.facade.ProductCommandFacade;
import com.github.thundax.bacon.product.api.request.ProductOrderSnapshotCreateFacadeRequest;
import com.github.thundax.bacon.product.api.response.ProductOrderSnapshotCreateFacadeResponse;
import com.github.thundax.bacon.product.application.command.CreateOrderProductSnapshotCommand;
import com.github.thundax.bacon.product.application.command.ProductSnapshotApplicationService;
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
    public ProductOrderSnapshotCreateFacadeResponse createOrderProductSnapshot(
            ProductOrderSnapshotCreateFacadeRequest request) {
        return new ProductOrderSnapshotCreateFacadeResponse(ProductInterfaceAssembler.toSnapshotDTO(
                productSnapshotApplicationService.createOrderProductSnapshot(new CreateOrderProductSnapshotCommand(
                        request.tenantId(),
                        request.orderNo(),
                        request.orderItemNo(),
                        request.skuId(),
                        request.quantity()))));
    }
}
