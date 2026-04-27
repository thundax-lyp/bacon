package com.github.thundax.bacon.product.interfaces.assembler;

import com.github.thundax.bacon.product.api.dto.ProductSkuSaleInfoDTO;
import com.github.thundax.bacon.product.api.dto.ProductSnapshotDTO;
import com.github.thundax.bacon.product.application.command.CreateOrderProductSnapshotCommand;
import com.github.thundax.bacon.product.application.command.CreateProductCommand;
import com.github.thundax.bacon.product.application.command.CreateProductSkuCommand;
import com.github.thundax.bacon.product.application.command.UpdateProductCommand;
import com.github.thundax.bacon.product.application.result.ProductCommandResult;
import com.github.thundax.bacon.product.application.result.ProductSkuSaleInfoResult;
import com.github.thundax.bacon.product.application.result.ProductSnapshotResult;
import com.github.thundax.bacon.product.interfaces.request.CreateOrderProductSnapshotRequest;
import com.github.thundax.bacon.product.interfaces.request.CreateProductRequest;
import com.github.thundax.bacon.product.interfaces.request.CreateProductSkuRequest;
import com.github.thundax.bacon.product.interfaces.request.UpdateProductRequest;
import com.github.thundax.bacon.product.interfaces.response.ProductCommandResponse;
import com.github.thundax.bacon.product.interfaces.response.ProductSkuSaleInfoResponse;
import com.github.thundax.bacon.product.interfaces.response.ProductSnapshotResponse;
import java.time.Instant;
import java.util.List;

public final class ProductInterfaceAssembler {

    private ProductInterfaceAssembler() {}

    public static CreateProductCommand toCreateCommand(Long tenantId, CreateProductRequest request) {
        return new CreateProductCommand(
                tenantId,
                request.spuCode(),
                request.spuName(),
                request.categoryId(),
                request.description(),
                request.mainImageObjectId(),
                toSkuCommands(request.skus()),
                request.idempotencyKey());
    }

    public static UpdateProductCommand toUpdateCommand(Long tenantId, Long spuId, UpdateProductRequest request) {
        return new UpdateProductCommand(
                tenantId,
                spuId,
                request.spuName(),
                request.categoryId(),
                request.description(),
                request.mainImageObjectId(),
                request.expectedVersion(),
                request.idempotencyKey());
    }

    public static CreateOrderProductSnapshotCommand toSnapshotCommand(
            Long tenantId, CreateOrderProductSnapshotRequest request) {
        return new CreateOrderProductSnapshotCommand(
                tenantId, request.orderNo(), request.orderItemNo(), request.skuId(), request.quantity());
    }

    public static ProductCommandResponse toCommandResponse(ProductCommandResult result) {
        return new ProductCommandResponse(
                result.tenantId(),
                result.spuId(),
                result.spuCode(),
                result.productStatus().value(),
                result.productVersion());
    }

    public static ProductSkuSaleInfoDTO toSkuSaleInfoDTO(ProductSkuSaleInfoResult result) {
        return new ProductSkuSaleInfoDTO(
                result.tenantId(),
                result.spuId(),
                result.spuCode(),
                result.spuName(),
                result.skuId(),
                result.skuCode(),
                result.skuName(),
                result.categoryId(),
                result.categoryName(),
                result.specAttributes(),
                result.salePrice(),
                result.mainImageObjectId(),
                result.productStatus(),
                result.skuStatus(),
                result.productVersion(),
                result.saleable(),
                result.failureReason());
    }

    public static ProductSkuSaleInfoResponse toSkuSaleInfoResponse(ProductSkuSaleInfoResult result) {
        return new ProductSkuSaleInfoResponse(
                result.tenantId(),
                result.spuId(),
                result.spuCode(),
                result.spuName(),
                result.skuId(),
                result.skuCode(),
                result.skuName(),
                result.categoryId(),
                result.categoryName(),
                result.specAttributes(),
                result.salePrice(),
                result.mainImageObjectId(),
                result.productStatus(),
                result.skuStatus(),
                result.productVersion(),
                result.saleable(),
                result.failureReason());
    }

    public static ProductSnapshotDTO toSnapshotDTO(ProductSnapshotResult result) {
        return new ProductSnapshotDTO(
                result.tenantId(),
                result.snapshotId(),
                result.orderNo(),
                result.orderItemNo(),
                result.spuId(),
                result.spuCode(),
                result.spuName(),
                result.skuId(),
                result.skuCode(),
                result.skuName(),
                result.categoryId(),
                result.categoryName(),
                result.specAttributes(),
                result.salePrice(),
                result.quantity(),
                result.mainImageObjectId(),
                result.productVersion(),
                Instant.now());
    }

    public static ProductSnapshotResponse toSnapshotResponse(ProductSnapshotResult result) {
        return new ProductSnapshotResponse(
                result.tenantId(),
                result.snapshotId(),
                result.orderNo(),
                result.orderItemNo(),
                result.spuId(),
                result.spuCode(),
                result.spuName(),
                result.skuId(),
                result.skuCode(),
                result.skuName(),
                result.categoryId(),
                result.categoryName(),
                result.specAttributes(),
                result.salePrice(),
                result.quantity(),
                result.mainImageObjectId(),
                result.productVersion());
    }

    private static List<CreateProductSkuCommand> toSkuCommands(List<CreateProductSkuRequest> requests) {
        return requests.stream()
                .map(request -> new CreateProductSkuCommand(
                        request.skuCode(), request.skuName(), request.specAttributes(), request.salePrice()))
                .toList();
    }
}
