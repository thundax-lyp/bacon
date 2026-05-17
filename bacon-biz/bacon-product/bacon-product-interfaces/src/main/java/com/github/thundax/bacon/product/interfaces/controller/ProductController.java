package com.github.thundax.bacon.product.interfaces.controller;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.product.application.command.ChangeProductStatusCommand;
import com.github.thundax.bacon.product.application.command.ProductManagementApplicationService;
import com.github.thundax.bacon.product.interfaces.assembler.ProductInterfaceAssembler;
import com.github.thundax.bacon.product.interfaces.request.ChangeProductStatusRequest;
import com.github.thundax.bacon.product.interfaces.request.CreateProductRequest;
import com.github.thundax.bacon.product.interfaces.request.UpdateProductRequest;
import com.github.thundax.bacon.product.interfaces.response.ProductCommandResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@WrappedApiController
@RequestMapping("/product/products")
@Tag(name = "Product-Management", description = "Product 域管理接口")
public class ProductController {

    private final ProductManagementApplicationService productManagementApplicationService;

    public ProductController(ProductManagementApplicationService productManagementApplicationService) {
        this.productManagementApplicationService = productManagementApplicationService;
    }

    @Operation(summary = "创建商品")
    @HasPermission("product:product:create")
    @PostMapping
    public ProductCommandResponse create(@Valid @RequestBody CreateProductRequest request) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return ProductInterfaceAssembler.toCommandResponse(productManagementApplicationService.createProduct(
                ProductInterfaceAssembler.toCreateCommand(tenantId, request)));
    }

    @Operation(summary = "编辑商品")
    @HasPermission("product:product:update")
    @PutMapping("/{spuId}")
    public ProductCommandResponse update(
            @PathVariable("spuId") Long spuId, @Valid @RequestBody UpdateProductRequest request) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return ProductInterfaceAssembler.toCommandResponse(productManagementApplicationService.updateProduct(
                ProductInterfaceAssembler.toUpdateCommand(tenantId, spuId, request)));
    }

    @Operation(summary = "商品上架")
    @HasPermission("product:product:update")
    @PutMapping("/{spuId}/on-sale")
    public ProductCommandResponse onSale(
            @PathVariable("spuId") Long spuId, @Valid @RequestBody ChangeProductStatusRequest request) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return ProductInterfaceAssembler.toCommandResponse(productManagementApplicationService.onSale(
                new ChangeProductStatusCommand(tenantId, spuId, request.expectedVersion(), null)));
    }

    @Operation(summary = "商品下架")
    @HasPermission("product:product:update")
    @PutMapping("/{spuId}/off-sale")
    public ProductCommandResponse offSale(
            @PathVariable("spuId") Long spuId, @Valid @RequestBody ChangeProductStatusRequest request) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return ProductInterfaceAssembler.toCommandResponse(productManagementApplicationService.offSale(
                new ChangeProductStatusCommand(tenantId, spuId, request.expectedVersion(), null)));
    }

    @Operation(summary = "商品归档")
    @HasPermission("product:product:archive")
    @PutMapping("/{spuId}/archive")
    public ProductCommandResponse archive(
            @PathVariable("spuId") Long spuId, @Valid @RequestBody ChangeProductStatusRequest request) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return ProductInterfaceAssembler.toCommandResponse(productManagementApplicationService.archive(
                new ChangeProductStatusCommand(tenantId, spuId, request.expectedVersion(), null)));
    }
}
