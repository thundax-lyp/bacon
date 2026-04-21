package com.github.thundax.bacon.inventory.interfaces.assembler;

import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationItemFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.application.command.InventoryCreateCommand;
import com.github.thundax.bacon.inventory.application.command.InventoryDeductStockCommand;
import com.github.thundax.bacon.inventory.application.command.InventoryReleaseStockCommand;
import com.github.thundax.bacon.inventory.application.command.InventoryReservationItemCommand;
import com.github.thundax.bacon.inventory.application.command.InventoryReserveStockCommand;
import com.github.thundax.bacon.inventory.application.command.InventoryStatusUpdateCommand;
import com.github.thundax.bacon.inventory.application.query.InventoryAuditDeadLetterPageQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryAuditLogQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryAvailableStockQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryBatchAvailableStockQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryLedgerQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryPageQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryReservationQuery;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.interfaces.request.CreateInventoryRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryAuditDeadLetterPageRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryBatchQueryRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryPageRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryStatusUpdateRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class InventoryInterfaceAssembler {

    private InventoryInterfaceAssembler() {}

    public static InventoryCreateCommand toCreateCommand(CreateInventoryRequest request) {
        return new InventoryCreateCommand(
                request == null ? null : SkuIdCodec.toDomain(request.skuId()),
                request == null ? null : request.onHandQuantity(),
                request == null || request.status() == null ? null : InventoryStatus.from(request.status()));
    }

    public static InventoryStatusUpdateCommand toStatusUpdateCommand(
            Long skuId, InventoryStatusUpdateRequest request) {
        return new InventoryStatusUpdateCommand(
                SkuIdCodec.toDomain(skuId),
                request == null || request.status() == null ? null : InventoryStatus.from(request.status()));
    }

    public static InventoryAvailableStockQuery toAvailableStockQuery(Long skuId) {
        return new InventoryAvailableStockQuery(SkuIdCodec.toDomain(skuId));
    }

    public static InventoryBatchAvailableStockQuery toBatchAvailableStockQuery(InventoryBatchQueryRequest request) {
        Set<SkuId> skuIds =
                request == null || request.getSkuIds() == null
                        ? Set.of()
                        : request.getSkuIds().stream().map(SkuIdCodec::toDomain).collect(Collectors.toSet());
        return new InventoryBatchAvailableStockQuery(skuIds);
    }

    public static InventoryPageQuery toPageQuery(InventoryPageRequest request) {
        return new InventoryPageQuery(
                request == null ? null : SkuIdCodec.toDomain(request.getSkuId()),
                request == null || request.getStatus() == null ? null : InventoryStatus.from(request.getStatus()),
                request == null ? null : request.getPageNo(),
                request == null ? null : request.getPageSize());
    }

    public static InventoryReservationQuery toReservationQuery(String orderNo) {
        return new InventoryReservationQuery(OrderNoCodec.toDomain(orderNo));
    }

    public static InventoryLedgerQuery toLedgerQuery(String orderNo) {
        return new InventoryLedgerQuery(OrderNoCodec.toDomain(orderNo));
    }

    public static InventoryAuditLogQuery toAuditLogQuery(String orderNo) {
        return new InventoryAuditLogQuery(OrderNoCodec.toDomain(orderNo));
    }

    public static InventoryAuditDeadLetterPageQuery toAuditDeadLetterPageQuery(InventoryAuditDeadLetterPageRequest request) {
        return new InventoryAuditDeadLetterPageQuery(
                request == null ? null : OrderNoCodec.toDomain(request.getOrderNo()),
                request == null || request.getReplayStatus() == null
                        ? null
                        : InventoryAuditReplayStatus.from(request.getReplayStatus()),
                request == null ? null : request.getPageNo(),
                request == null ? null : request.getPageSize());
    }

    public static InventoryReserveStockCommand toReserveCommand(InventoryReserveFacadeRequest request) {
        return new InventoryReserveStockCommand(
                request == null ? null : OrderNoCodec.toDomain(request.getOrderNo()),
                request == null || request.getItems() == null
                        ? List.of()
                        : request.getItems().stream().map(InventoryInterfaceAssembler::toReservationItemCommand).toList());
    }

    public static InventoryReleaseStockCommand toReleaseCommand(InventoryReleaseFacadeRequest request) {
        return new InventoryReleaseStockCommand(
                request == null ? null : OrderNoCodec.toDomain(request.getOrderNo()),
                request == null || request.getReason() == null ? null : InventoryReleaseReason.from(request.getReason()));
    }

    public static InventoryDeductStockCommand toDeductCommand(InventoryDeductFacadeRequest request) {
        return new InventoryDeductStockCommand(
                request == null ? null : OrderNoCodec.toDomain(request.getOrderNo()));
    }

    public static InventoryAvailableStockQuery toAvailableStockQuery(InventoryAvailableStockFacadeRequest request) {
        return new InventoryAvailableStockQuery(SkuIdCodec.toDomain(request.getSkuId()));
    }

    public static InventoryBatchAvailableStockQuery toBatchAvailableStockQuery(
            InventoryBatchAvailableStockFacadeRequest request) {
        Set<SkuId> skuIds =
                request == null || request.getSkuIds() == null
                        ? Set.of()
                        : request.getSkuIds().stream().map(SkuIdCodec::toDomain).collect(Collectors.toSet());
        return new InventoryBatchAvailableStockQuery(skuIds);
    }

    public static InventoryReservationQuery toReservationQuery(InventoryReservationGetFacadeRequest request) {
        return new InventoryReservationQuery(request == null ? null : OrderNoCodec.toDomain(request.getOrderNo()));
    }

    private static InventoryReservationItemCommand toReservationItemCommand(InventoryReservationItemFacadeRequest request) {
        return new InventoryReservationItemCommand(
                request == null ? null : SkuIdCodec.toDomain(request.getSkuId()),
                request == null ? null : request.getQuantity());
    }
}
