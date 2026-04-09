package com.github.thundax.bacon.inventory.interfaces.provider;

import com.github.thundax.bacon.common.commerce.mapper.SkuIdMapper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReleaseCommandDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReserveCommandDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.command.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
@RequestMapping("/providers/inventory")
@Tag(name = "Inner-Inventory-Management", description = "Inventory 域内部 Provider 接口")
public class InventoryProviderController {

    private final InventoryQueryApplicationService inventoryQueryService;
    private final InventoryApplicationService inventoryApplicationService;

    public InventoryProviderController(
            InventoryQueryApplicationService inventoryQueryService,
            InventoryApplicationService inventoryApplicationService) {
        this.inventoryQueryService = inventoryQueryService;
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @Operation(summary = "查询 SKU 可用库存")
    @GetMapping("/stocks/{skuId}")
    public InventoryStockDTO getAvailableStock(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId, @PathVariable @NotNull @Positive Long skuId) {
        return inventoryQueryService.getAvailableStock(TenantId.of(tenantId), SkuIdMapper.toDomain(skuId));
    }

    @Operation(summary = "批量查询 SKU 可用库存")
    @GetMapping("/stocks")
    public List<InventoryStockDTO> batchGetAvailableStock(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId,
            @RequestParam("skuIds") @NotNull Set<@NotNull @Positive Long> skuIds) {
        return inventoryQueryService.batchGetAvailableStock(
                TenantId.of(tenantId),
                skuIds.stream().map(SkuIdMapper::toDomain).collect(Collectors.toSet()));
    }

    @Operation(summary = "按订单号查询库存预占结果")
    @GetMapping("/reservations/{orderNo}")
    public InventoryReservationDTO getReservation(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId, @PathVariable @NotBlank String orderNo) {
        return inventoryQueryService.getReservationByOrderNo(TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo));
    }

    @Operation(summary = "按订单号查询库存流水")
    @GetMapping("/ledgers")
    public List<InventoryLedgerDTO> listLedgers(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId,
            @RequestParam("orderNo") @NotBlank String orderNo) {
        return inventoryQueryService.listLedgersByOrderNo(TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo));
    }

    @Operation(summary = "按订单号查询库存审计日志")
    @GetMapping("/audit-logs")
    public List<InventoryAuditLogDTO> listAuditLogs(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId,
            @RequestParam("orderNo") @NotBlank String orderNo) {
        return inventoryQueryService.listAuditLogsByOrderNo(TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo));
    }

    @Operation(summary = "预占库存")
    @PostMapping("/reservations/{orderNo}/reserve")
    public InventoryReservationResultDTO reserve(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId,
            @PathVariable @NotBlank String orderNo,
            @Valid @RequestBody InventoryReserveCommandDTO request) {
        return inventoryApplicationService.reserveStock(
                TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo), request.getItems());
    }

    @Operation(summary = "释放预占库存")
    @PostMapping("/reservations/{orderNo}/release")
    public InventoryReservationResultDTO release(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId,
            @PathVariable @NotBlank String orderNo,
            @Valid @RequestBody InventoryReleaseCommandDTO request) {
        return inventoryApplicationService.releaseReservedStock(
                TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo), toReleaseReason(request.getReason()));
    }

    @Operation(summary = "扣减预占库存")
    @PostMapping("/reservations/{orderNo}/deduct")
    public InventoryReservationResultDTO deduct(
            @RequestParam("tenantId") @NotNull @Positive Long tenantId, @PathVariable @NotBlank String orderNo) {
        return inventoryApplicationService.deductReservedStock(TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo));
    }

    private InventoryReleaseReason toReleaseReason(String reason) {
        try {
            return InventoryReleaseReason.from(reason);
        } catch (IllegalArgumentException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_RELEASE_REASON, reason);
        }
    }
}
