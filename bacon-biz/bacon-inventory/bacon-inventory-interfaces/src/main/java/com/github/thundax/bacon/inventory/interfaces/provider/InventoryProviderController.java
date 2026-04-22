package com.github.thundax.bacon.inventory.interfaces.provider;

import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryInterfaceAssembler;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryBatchQueryRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryDeductRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryOrderScopedRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryReleaseRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryReserveRequest;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryAuditLogResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryLedgerResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryReservationResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryStockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/providers/inventory")
@Tag(name = "Inner-Inventory-Management", description = "Inventory 域内部 Provider 接口")
public class InventoryProviderController {

    private final InventoryQueryApplicationService inventoryQueryService;
    private final InventoryCommandApplicationService inventoryCommandApplicationService;

    public InventoryProviderController(
            InventoryQueryApplicationService inventoryQueryService,
            InventoryCommandApplicationService inventoryCommandApplicationService) {
        this.inventoryQueryService = inventoryQueryService;
        this.inventoryCommandApplicationService = inventoryCommandApplicationService;
    }

    @Operation(summary = "查询 SKU 可用库存")
    @GetMapping("/stocks/{skuId}")
    public InventoryStockResponse getAvailableStock(@PathVariable @NotNull @Positive Long skuId) {
        return InventoryStockResponse.from(
                inventoryQueryService.getAvailableStock(InventoryInterfaceAssembler.toAvailableStockQuery(skuId)));
    }

    @Operation(summary = "批量查询 SKU 可用库存")
    @GetMapping("/stocks")
    public List<InventoryStockResponse> batchGetAvailableStock(@Valid InventoryBatchQueryRequest request) {
        return inventoryQueryService
                .batchGetAvailableStock(InventoryInterfaceAssembler.toBatchAvailableStockQuery(request))
                .stream()
                .map(InventoryStockResponse::from)
                .toList();
    }

    @Operation(summary = "按订单号查询库存预占结果")
    @GetMapping("/reservations/{orderNo}")
    public InventoryReservationResponse getReservation(@PathVariable @NotBlank String orderNo) {
        return InventoryReservationResponse.from(
                inventoryQueryService.getReservationByOrderNo(InventoryInterfaceAssembler.toReservationQuery(orderNo)));
    }

    @Operation(summary = "按订单号查询库存流水")
    @GetMapping("/ledgers")
    public List<InventoryLedgerResponse> listLedgers(@Valid InventoryOrderScopedRequest request) {
        return inventoryQueryService.listLedgersByOrderNo(InventoryInterfaceAssembler.toLedgerQuery(request.getOrderNo()))
                .stream()
                .map(InventoryLedgerResponse::from)
                .toList();
    }

    @Operation(summary = "按订单号查询库存审计日志")
    @GetMapping("/audit-logs")
    public List<InventoryAuditLogResponse> listAuditLogs(@Valid InventoryOrderScopedRequest request) {
        return inventoryQueryService
                .listAuditLogsByOrderNo(InventoryInterfaceAssembler.toAuditLogQuery(request.getOrderNo()))
                .stream()
                .map(InventoryAuditLogResponse::from)
                .toList();
    }

    @Operation(summary = "预占库存")
    @PostMapping("/reservations/reserve")
    public InventoryReservationResponse reserve(@Valid @RequestBody InventoryReserveRequest request) {
        return InventoryReservationResponse.from(
                inventoryCommandApplicationService.reserveStock(
                        InventoryInterfaceAssembler.toReserveCommand(request)));
    }

    @Operation(summary = "释放预占库存")
    @PostMapping("/reservations/release")
    public InventoryReservationResponse release(@Valid @RequestBody InventoryReleaseRequest request) {
        return InventoryReservationResponse.from(
                inventoryCommandApplicationService.releaseReservedStock(
                        InventoryInterfaceAssembler.toReleaseCommand(request)));
    }

    @Operation(summary = "扣减预占库存")
    @PostMapping("/reservations/deduct")
    public InventoryReservationResponse deduct(@Valid @RequestBody InventoryDeductRequest request) {
        return InventoryReservationResponse.from(
                inventoryCommandApplicationService.deductReservedStock(
                        InventoryInterfaceAssembler.toDeductCommand(request)));
    }
}
