package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDataObject;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDataObject;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryLedgerDataObject;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationDataObject;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationItemDataObject;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditLogMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryLedgerMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationItemMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryMapper inventoryMapper;
    private final InventoryReservationMapper reservationMapper;
    private final InventoryReservationItemMapper reservationItemMapper;
    private final InventoryLedgerMapper ledgerMapper;
    private final InventoryAuditLogMapper auditLogMapper;

    public InventoryRepositoryImpl(InventoryMapper inventoryMapper,
                                   InventoryReservationMapper reservationMapper,
                                   InventoryReservationItemMapper reservationItemMapper,
                                   InventoryLedgerMapper ledgerMapper,
                                   InventoryAuditLogMapper auditLogMapper) {
        this.inventoryMapper = inventoryMapper;
        this.reservationMapper = reservationMapper;
        this.reservationItemMapper = reservationItemMapper;
        this.ledgerMapper = ledgerMapper;
        this.auditLogMapper = auditLogMapper;
        log.info("Using MyBatis-Plus inventory repository");
    }

    @Override
    public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
        return Optional.ofNullable(inventoryMapper.selectOne(Wrappers.<InventoryDataObject>lambdaQuery()
                .eq(InventoryDataObject::getTenantId, tenantId)
                .eq(InventoryDataObject::getSkuId, skuId)))
                .map(this::toDomain);
    }

    @Override
    public List<Inventory> findInventories(Long tenantId) {
        return inventoryMapper.selectList(Wrappers.<InventoryDataObject>lambdaQuery()
                        .eq(InventoryDataObject::getTenantId, tenantId)
                        .orderByAsc(InventoryDataObject::getSkuId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        return inventoryMapper.selectList(Wrappers.<InventoryDataObject>lambdaQuery()
                        .eq(InventoryDataObject::getTenantId, tenantId)
                        .in(InventoryDataObject::getSkuId, skuIds)
                        .orderByAsc(InventoryDataObject::getSkuId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Inventory saveInventory(Inventory inventory) {
        InventoryDataObject dataObject = toDataObject(inventory);
        if (dataObject.getId() == null) {
            inventoryMapper.insert(dataObject);
        } else {
            inventoryMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    @Override
    public InventoryReservation saveReservation(InventoryReservation reservation) {
        InventoryReservationDataObject reservationDataObject = toDataObject(reservation);
        if (reservationDataObject.getId() == null) {
            reservationMapper.insert(reservationDataObject);
            List<InventoryReservationItemDataObject> itemDataObjects = reservation.getItems().stream()
                    .map(item -> toDataObject(item, reservation.getTenantId(), reservation.getReservationNo()))
                    .toList();
            itemDataObjects.forEach(reservationItemMapper::insert);
        } else {
            reservationMapper.updateById(reservationDataObject);
        }
        return findReservation(reservation.getTenantId(), reservation.getOrderNo()).orElseThrow();
    }

    @Override
    public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
        InventoryReservationDataObject reservation = reservationMapper.selectOne(
                Wrappers.<InventoryReservationDataObject>lambdaQuery()
                        .eq(InventoryReservationDataObject::getTenantId, tenantId)
                        .eq(InventoryReservationDataObject::getOrderNo, orderNo));
        if (reservation == null) {
            return Optional.empty();
        }
        List<InventoryReservationItem> items = reservationItemMapper.selectList(
                        Wrappers.<InventoryReservationItemDataObject>lambdaQuery()
                                .eq(InventoryReservationItemDataObject::getTenantId, tenantId)
                                .eq(InventoryReservationItemDataObject::getReservationNo, reservation.getReservationNo())
                                .orderByAsc(InventoryReservationItemDataObject::getSkuId))
                .stream()
                .map(this::toDomain)
                .toList();
        return Optional.of(toDomain(reservation, items));
    }

    @Override
    public void saveLedger(InventoryLedger ledger) {
        ledgerMapper.insert(toDataObject(ledger));
    }

    @Override
    public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
        return ledgerMapper.selectList(Wrappers.<InventoryLedgerDataObject>lambdaQuery()
                        .eq(InventoryLedgerDataObject::getTenantId, tenantId)
                        .eq(InventoryLedgerDataObject::getOrderNo, orderNo)
                        .orderByAsc(InventoryLedgerDataObject::getOccurredAt, InventoryLedgerDataObject::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveAuditLog(InventoryAuditLog auditLog) {
        auditLogMapper.insert(toDataObject(auditLog));
    }

    @Override
    public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return auditLogMapper.selectList(Wrappers.<InventoryAuditLogDataObject>lambdaQuery()
                        .eq(InventoryAuditLogDataObject::getTenantId, tenantId)
                        .eq(InventoryAuditLogDataObject::getOrderNo, orderNo)
                        .orderByAsc(InventoryAuditLogDataObject::getOccurredAt, InventoryAuditLogDataObject::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Inventory toDomain(InventoryDataObject dataObject) {
        return new Inventory(dataObject.getId(), dataObject.getTenantId(), dataObject.getSkuId(), dataObject.getWarehouseId(),
                dataObject.getOnHandQuantity(), dataObject.getReservedQuantity(), dataObject.getAvailableQuantity(),
                dataObject.getStatus(), dataObject.getUpdatedAt() == null ? dataObject.getCreatedAt() : dataObject.getUpdatedAt());
    }

    private InventoryDataObject toDataObject(Inventory inventory) {
        return new InventoryDataObject(inventory.getId(), inventory.getTenantId(), inventory.getSkuId(),
                inventory.getWarehouseId(), inventory.getOnHandQuantity(), inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(), inventory.getStatus(), null, inventory.getUpdatedAt(), null,
                inventory.getUpdatedAt());
    }

    private InventoryReservation toDomain(InventoryReservationDataObject reservation, List<InventoryReservationItem> items) {
        InventoryReservation domain = new InventoryReservation(reservation.getId(), reservation.getTenantId(),
                reservation.getReservationNo(), reservation.getOrderNo(), reservation.getWarehouseId(),
                reservation.getCreatedAt(), items);
        return switch (reservation.getReservationStatus()) {
            case InventoryReservation.STATUS_CREATED -> domain;
            case InventoryReservation.STATUS_RESERVED -> {
                domain.reserve();
                yield domain;
            }
            case InventoryReservation.STATUS_FAILED -> {
                domain.fail(reservation.getFailureReason());
                yield domain;
            }
            case InventoryReservation.STATUS_RELEASED -> {
                domain.reserve();
                domain.release(reservation.getReleaseReason(), reservation.getReleasedAt());
                yield domain;
            }
            case InventoryReservation.STATUS_DEDUCTED -> {
                domain.reserve();
                domain.deduct(reservation.getDeductedAt());
                yield domain;
            }
            default -> throw new IllegalStateException("UNKNOWN_RESERVATION_STATUS:" + reservation.getReservationStatus());
        };
    }

    private InventoryReservationItem toDomain(InventoryReservationItemDataObject item) {
        return new InventoryReservationItem(item.getId(), item.getTenantId(), item.getReservationNo(), item.getSkuId(),
                item.getQuantity());
    }

    private InventoryReservationDataObject toDataObject(InventoryReservation reservation) {
        return new InventoryReservationDataObject(reservation.getId(), reservation.getTenantId(),
                reservation.getReservationNo(), reservation.getOrderNo(), reservation.getReservationStatus(),
                reservation.getWarehouseId(), reservation.getFailureReason(), reservation.getReleaseReason(),
                reservation.getCreatedAt(), reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryReservationItemDataObject toDataObject(InventoryReservationItem item, Long tenantId, String reservationNo) {
        return new InventoryReservationItemDataObject(item.getId(), tenantId, reservationNo, item.getSkuId(), item.getQuantity());
    }

    private InventoryLedger toDomain(InventoryLedgerDataObject dataObject) {
        return new InventoryLedger(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getSkuId(), dataObject.getWarehouseId(),
                dataObject.getLedgerType(), dataObject.getQuantity(), dataObject.getOccurredAt());
    }

    private InventoryLedgerDataObject toDataObject(InventoryLedger ledger) {
        return new InventoryLedgerDataObject(ledger.getId(), ledger.getTenantId(), ledger.getOrderNo(),
                ledger.getReservationNo(), ledger.getSkuId(), ledger.getWarehouseId(), ledger.getLedgerType(),
                ledger.getQuantity(), ledger.getOccurredAt());
    }

    private InventoryAuditLog toDomain(InventoryAuditLogDataObject dataObject) {
        return new InventoryAuditLog(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getActionType(), dataObject.getOperatorType(),
                dataObject.getOperatorId(), dataObject.getOccurredAt());
    }

    private InventoryAuditLogDataObject toDataObject(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDataObject(auditLog.getId(), auditLog.getTenantId(), auditLog.getOrderNo(),
                auditLog.getReservationNo(), auditLog.getActionType(), auditLog.getOperatorType(),
                auditLog.getOperatorId(), auditLog.getOccurredAt());
    }
}
