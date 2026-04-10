package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.AvailableQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存主数据领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Inventory {

    /** 库存主键。 */
    private InventoryId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 商品 SKU 主键。 */
    private SkuId skuId;
    /** 仓库业务编码。 */
    private WarehouseCode warehouseCode;
    /** 在库数量。 */
    private OnHandQuantity onHandQuantity;
    /** 预占数量。 */
    private ReservedQuantity reservedQuantity;
    /** 库存状态。 */
    private InventoryStatus status;
    /** 乐观锁版本号。 */
    private Version version;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static Inventory create(
            InventoryId id,
            TenantId tenantId,
            SkuId skuId,
            WarehouseCode warehouseCode,
            OnHandQuantity onHandQuantity) {
        if (Objects.isNull(id) || Objects.isNull(tenantId) || Objects.isNull(skuId) || Objects.isNull(warehouseCode)) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_INVENTORY_KEY);
        }
        if (Objects.isNull(onHandQuantity)) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_ON_HAND_QUANTITY, "null");
        }
        return new Inventory(
                id,
                tenantId,
                skuId,
                warehouseCode,
                onHandQuantity,
                new ReservedQuantity(0),
                InventoryStatus.ENABLED,
                new Version(0L),
                Instant.now());
    }

    public static Inventory reconstruct(
            InventoryId id,
            TenantId tenantId,
            SkuId skuId,
            WarehouseCode warehouseCode,
            OnHandQuantity onHandQuantity,
            ReservedQuantity reservedQuantity,
            InventoryStatus status,
            Version version,
            Instant updatedAt) {
        if (Objects.isNull(onHandQuantity)) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_ON_HAND_QUANTITY, "null");
        }
        if (Objects.isNull(reservedQuantity)) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_QUANTITY, "null");
        }
        if (!onHandQuantity.isEnough(reservedQuantity.value())) {
            throw new InventoryDomainException(
                    InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH,
                    onHandQuantity.value() + " < " + reservedQuantity.value());
        }
        if (Objects.isNull(version)) {
            throw new IllegalArgumentException("version must not be null");
        }
        return new Inventory(
                id,
                tenantId,
                skuId,
                warehouseCode,
                onHandQuantity,
                reservedQuantity,
                status,
                version,
                updatedAt);
    }

    public AvailableQuantity availableQuantity() {
        return new AvailableQuantity(onHandQuantity.value() - reservedQuantity.value());
    }

    public void increaseStock(int delta) {
        onHandQuantity = onHandQuantity.increase(delta);
        refreshUpdatedAt();
    }

    public void reserve(int quantity) {
        // 预占只减少可用量，不减少实物在库量；真正扣减要等订单支付成功后单独执行。
        if (InventoryStatus.DISABLED.equals(status)) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_DISABLED, String.valueOf(skuId.value()));
        }
        if (!availableQuantity().isEnough(quantity)) {
            throw new InventoryDomainException(InventoryErrorCode.INSUFFICIENT_STOCK, String.valueOf(skuId));
        }
        reservedQuantity = reservedQuantity.increase(quantity);
        refreshUpdatedAt();
    }

    public void release(int quantity) {
        // 释放库存只归还已预占量，不能把尚未预占的数量“释放”回去。
        if (!reservedQuantity.isEnough(quantity)) {
            throw new InventoryDomainException(InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH, String.valueOf(skuId));
        }
        reservedQuantity = reservedQuantity.decrease(quantity);
        refreshUpdatedAt();
    }

    public void deduct(int quantity) {
        // 扣减要求同时满足“已预占”和“在库足够”，避免支付成功后把未锁定库存直接扣成负数。
        if (!reservedQuantity.isEnough(quantity)) {
            throw new InventoryDomainException(InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH, String.valueOf(skuId));
        }
        if (!onHandQuantity.isEnough(quantity)) {
            throw new InventoryDomainException(InventoryErrorCode.ON_HAND_QUANTITY_NOT_ENOUGH, String.valueOf(skuId));
        }
        reservedQuantity = reservedQuantity.decrease(quantity);
        onHandQuantity = onHandQuantity.decrease(quantity);
        refreshUpdatedAt();
    }

    public void updateStatus(InventoryStatus targetStatus) {
        // 启停库存是运维级操作，不改数量，只更新状态和时间戳。
        this.status = targetStatus;
        refreshUpdatedAt();
    }

    public void markPersisted(Version persistedVersion) {
        // 乐观锁版本由持久化层写回，领域对象本身不自增，避免和数据库版本漂移。
        this.version = persistedVersion;
    }

    private void refreshUpdatedAt() {
        updatedAt = Instant.now();
    }
}
