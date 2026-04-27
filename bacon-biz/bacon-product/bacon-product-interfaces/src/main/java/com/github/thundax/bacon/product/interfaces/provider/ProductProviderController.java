package com.github.thundax.bacon.product.interfaces.provider;

import com.github.thundax.bacon.product.application.command.CreateOrderProductSnapshotCommand;
import com.github.thundax.bacon.product.application.service.ProductSearchApplicationService;
import com.github.thundax.bacon.product.application.service.ProductSnapshotApplicationService;
import com.github.thundax.bacon.product.interfaces.assembler.ProductInterfaceAssembler;
import com.github.thundax.bacon.product.interfaces.request.CreateOrderProductSnapshotRequest;
import com.github.thundax.bacon.product.interfaces.response.ProductSkuSaleInfoResponse;
import com.github.thundax.bacon.product.interfaces.response.ProductSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/providers/products")
@Tag(name = "Inner-Product-Management", description = "Product 域内部 Provider 接口")
public class ProductProviderController {

    private final ProductSearchApplicationService productSearchApplicationService;
    private final ProductSnapshotApplicationService productSnapshotApplicationService;

    public ProductProviderController(
            ProductSearchApplicationService productSearchApplicationService,
            ProductSnapshotApplicationService productSnapshotApplicationService) {
        this.productSearchApplicationService = productSearchApplicationService;
        this.productSnapshotApplicationService = productSnapshotApplicationService;
    }

    @Operation(summary = "查询 SKU 销售信息")
    @GetMapping("/skus/{skuId}/sale-info")
    public ProductSkuSaleInfoResponse getSkuSaleInfo(
            @RequestParam("tenantId") @NotNull Long tenantId, @PathVariable("skuId") Long skuId) {
        return ProductInterfaceAssembler.toSkuSaleInfoResponse(productSearchApplicationService.getSkuSaleInfo(skuId));
    }

    @Operation(summary = "创建订单商品快照")
    @PostMapping("/snapshots/order-items")
    public ProductSnapshotResponse createOrderProductSnapshot(
            @RequestParam("tenantId") @NotNull Long tenantId,
            @Valid @RequestBody CreateOrderProductSnapshotRequest request) {
        return ProductInterfaceAssembler.toSnapshotResponse(
                productSnapshotApplicationService.createOrderProductSnapshot(
                        new CreateOrderProductSnapshotCommand(
                                tenantId, request.orderNo(), request.orderItemNo(), request.skuId(), request.quantity())));
    }
}
