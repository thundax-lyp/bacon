package com.github.thundax.bacon.inventory.interfaces.provider;

import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryInterfaceAssembler;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryReservationResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public InventoryStockFacadeResponse getAvailableStock(@PathVariable @NotNull @Positive Long skuId) {
        return InventoryReservationResponseAssembler.fromStockDto(
                inventoryQueryService.getAvailableStock(InventoryInterfaceAssembler.toAvailableStockQuery(skuId)));
    }

    @Operation(summary = "批量查询 SKU 可用库存")
    @GetMapping("/stocks")
    public InventoryStockListFacadeResponse batchGetAvailableStock(
            @RequestParam("skuIds") @NotNull Set<@NotNull @Positive Long> skuIds) {
                return InventoryReservationResponseAssembler.fromStockDtos(
                inventoryQueryService.batchGetAvailableStock(
                        InventoryInterfaceAssembler.toBatchAvailableStockQuery(
                                new InventoryBatchAvailableStockFacadeRequest(skuIds))));
    }

    @Operation(summary = "按订单号查询库存预占结果")
    @GetMapping("/reservations/{orderNo}")
    public InventoryReservationFacadeResponse getReservation(@PathVariable @NotBlank String orderNo) {
                return InventoryReservationResponseAssembler.fromDto(
                inventoryQueryService.getReservationByOrderNo(
                        InventoryInterfaceAssembler.toReservationQuery(
                                new InventoryReservationGetFacadeRequest(orderNo))));
    }

    @Operation(summary = "按订单号查询库存流水")
    @GetMapping("/ledgers")
    public List<InventoryLedgerDTO> listLedgers(@RequestParam("orderNo") @NotBlank String orderNo) {
        return inventoryQueryService.listLedgersByOrderNo(InventoryInterfaceAssembler.toLedgerQuery(orderNo));
    }

    @Operation(summary = "按订单号查询库存审计日志")
    @GetMapping("/audit-logs")
    public List<InventoryAuditLogDTO> listAuditLogs(@RequestParam("orderNo") @NotBlank String orderNo) {
        return inventoryQueryService.listAuditLogsByOrderNo(InventoryInterfaceAssembler.toAuditLogQuery(orderNo));
    }

    @Operation(summary = "预占库存")
    @PostMapping("/reservations/reserve")
    public InventoryReservationFacadeResponse reserve(@Valid @RequestBody InventoryReserveFacadeRequest request) {
        return InventoryReservationResponseAssembler.fromResult(
                inventoryCommandApplicationService.reserveStock(
                        InventoryInterfaceAssembler.toReserveCommand(request)));
    }

    @Operation(summary = "释放预占库存")
    @PostMapping("/reservations/release")
    public InventoryReservationFacadeResponse release(@Valid @RequestBody InventoryReleaseFacadeRequest request) {
        return InventoryReservationResponseAssembler.fromResult(
                inventoryCommandApplicationService.releaseReservedStock(
                        InventoryInterfaceAssembler.toReleaseCommand(request)));
    }

    @Operation(summary = "扣减预占库存")
    @PostMapping("/reservations/deduct")
    public InventoryReservationFacadeResponse deduct(@Valid @RequestBody InventoryDeductFacadeRequest request) {
        return InventoryReservationResponseAssembler.fromResult(
                inventoryCommandApplicationService.deductReservedStock(
                        InventoryInterfaceAssembler.toDeductCommand(request)));
    }
}
