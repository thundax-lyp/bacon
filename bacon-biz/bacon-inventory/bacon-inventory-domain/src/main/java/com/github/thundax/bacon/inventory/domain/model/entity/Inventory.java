package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存主数据领域实体。
 */
@Getter
@AllArgsConstructor
public class Inventory {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";
    public static final Long DEFAULT_WAREHOUSE_ID = 1L;

    /** 库存主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 商品 SKU 主键。 */
    private Long skuId;
    /** 仓库主键。 */
    private Long warehouseId;
    /** 在库数量。 */
    private Integer onHandQuantity;
    /** 预占数量。 */
    private Integer reservedQuantity;
    /** 可用数量。 */
    private Integer availableQuantity;
    /** 库存状态。 */
    private String status;
    /** 乐观锁版本号。 */
    private Long version;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static Inventory create(Long id, Long tenantId, Long skuId, Integer onHandQuantity, String status, Instant createdAt) {
        if (tenantId == null || skuId == null) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_INVENTORY_KEY);
        }
        if (onHandQuantity == null || onHandQuantity < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_ON_HAND_QUANTITY, String.valueOf(skuId));
        }
        String normalizedStatus = normalizeStatus(status);
        // 新建库存时把 reserved/available 一次性归位，后续所有数量变化都只通过领域方法推进。
        return new Inventory(id, tenantId, skuId, DEFAULT_WAREHOUSE_ID, onHandQuantity, 0, onHandQuantity,
                normalizedStatus, 0L, createdAt);
    }

    public void reserve(int quantity, Instant operatedAt) {
        // 预占只减少可用量，不减少实物在库量；真正扣减要等订单支付成功后单独执行。
        ensureReservable(quantity);
        reservedQuantity += quantity;
        refreshAvailableQuantity(operatedAt);
    }

    public void ensureReservable(int quantity) {
        // 预校验单独暴露给应用层，用于在批量预占前尽早失败，而不是部分修改后再回滚。
        validateQuantity(quantity);
        ensureEnabled();
        if (availableQuantity < quantity) {
            throw new InventoryDomainException(InventoryErrorCode.INSUFFICIENT_STOCK, String.valueOf(skuId));
        }
    }

    public void release(int quantity, Instant operatedAt) {
        // 释放库存只归还已预占量，不能把尚未预占的数量“释放”回去。
        validateQuantity(quantity);
        if (reservedQuantity < quantity) {
            throw new InventoryDomainException(InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH, String.valueOf(skuId));
        }
        reservedQuantity -= quantity;
        refreshAvailableQuantity(operatedAt);
    }

    public void deduct(int quantity, Instant operatedAt) {
        // 扣减要求同时满足“已预占”和“在库足够”，避免支付成功后把未锁定库存直接扣成负数。
        validateQuantity(quantity);
        if (reservedQuantity < quantity) {
            throw new InventoryDomainException(InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH, String.valueOf(skuId));
        }
        if (onHandQuantity < quantity) {
            throw new InventoryDomainException(InventoryErrorCode.ON_HAND_QUANTITY_NOT_ENOUGH, String.valueOf(skuId));
        }
        reservedQuantity -= quantity;
        onHandQuantity -= quantity;
        refreshAvailableQuantity(operatedAt);
    }

    public void updateStatus(String targetStatus, Instant operatedAt) {
        // 启停库存是运维级操作，不改数量，只更新状态和时间戳。
        this.status = normalizeStatus(targetStatus);
        this.updatedAt = operatedAt;
    }

    public void markPersisted(Long persistedVersion) {
        // 乐观锁版本由持久化层写回，领域对象本身不自增，避免和数据库版本漂移。
        this.version = persistedVersion;
    }

    private void ensureEnabled() {
        if (STATUS_DISABLED.equals(status)) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_DISABLED, String.valueOf(skuId));
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_QUANTITY, String.valueOf(skuId));
        }
    }

    private void refreshAvailableQuantity(Instant operatedAt) {
        // availableQuantity 始终由 onHand - reserved 推导，禁止在其他地方直接赋值。
        availableQuantity = onHandQuantity - reservedQuantity;
        updatedAt = operatedAt;
    }

    private static String normalizeStatus(String status) {
        if (STATUS_ENABLED.equals(status) || STATUS_DISABLED.equals(status)) {
            return status;
        }
        throw new InventoryDomainException(InventoryErrorCode.INVALID_INVENTORY_STATUS, String.valueOf(status));
    }
}
