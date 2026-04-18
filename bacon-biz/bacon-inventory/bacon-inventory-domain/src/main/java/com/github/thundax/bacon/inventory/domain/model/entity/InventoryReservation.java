package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存预占单领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryReservation {

    /** 预占单主键。 */
    private Long id;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 仓库业务编码。 */
    private WarehouseCode warehouseCode;
    /** 创建时间。 */
    private Instant createdAt;
    /** 预占明细列表。 */
    private List<InventoryReservationItem> items;
    /** 预占状态。 */
    private InventoryReservationStatus reservationStatus;
    /** 失败原因。 */
    private String failureReason;
    /** 释放原因。 */
    private InventoryReleaseReason releaseReason;
    /** 释放时间。 */
    private Instant releasedAt;
    /** 扣减时间。 */
    private Instant deductedAt;

    public static InventoryReservation create(
            Long id,
            ReservationNo reservationNo,
            OrderNo orderNo,
            WarehouseCode warehouseCode,
            Instant createdAt,
            List<InventoryReservationItem> items) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(reservationNo, "reservationNo must not be null");
        Objects.requireNonNull(orderNo, "orderNo must not be null");
        Objects.requireNonNull(warehouseCode, "warehouseCode must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(items, "items must not be null");
        return new InventoryReservation(
                id,
                reservationNo,
                orderNo,
                warehouseCode,
                createdAt,
                items,
                InventoryReservationStatus.CREATED,
                null,
                null,
                null,
                null);
    }

    public static InventoryReservation reconstruct(
            Long id,
            ReservationNo reservationNo,
            OrderNo orderNo,
            WarehouseCode warehouseCode,
            Instant createdAt,
            List<InventoryReservationItem> items,
            InventoryReservationStatus reservationStatus,
            String failureReason,
            InventoryReleaseReason releaseReason,
            Instant releasedAt,
            Instant deductedAt) {
        // 预占单回写时必须带回终态与原因字段，应用层会基于这些字段判断是否还能继续补偿或回放。
        return new InventoryReservation(
                id,
                reservationNo,
                orderNo,
                warehouseCode,
                createdAt,
                items,
                reservationStatus,
                failureReason,
                releaseReason,
                releasedAt,
                deductedAt);
    }

    public boolean isReserved() {
        return InventoryReservationStatus.RESERVED.equals(reservationStatus);
    }

    public boolean isProcessing() {
        return InventoryReservationStatus.CREATED.equals(reservationStatus);
    }

    public boolean isReleased() {
        return InventoryReservationStatus.RELEASED.equals(reservationStatus);
    }

    public boolean isDeducted() {
        return InventoryReservationStatus.DEDUCTED.equals(reservationStatus);
    }

    public boolean isFailed() {
        return InventoryReservationStatus.FAILED.equals(reservationStatus);
    }

    public void reserve() {
        // 预占单只允许从 CREATED 进入 RESERVED；一旦失败或终结，就不能再次复用同一预占单。
        ensureStatus(InventoryReservationStatus.CREATED);
        this.reservationStatus = InventoryReservationStatus.RESERVED;
    }

    public void fail(String reason) {
        // 预占失败只允许发生在初始处理阶段，避免把已成功预占的单据重新打回失败。
        ensureStatus(InventoryReservationStatus.CREATED);
        this.reservationStatus = InventoryReservationStatus.FAILED;
        this.failureReason = reason;
    }

    public void release(InventoryReleaseReason reason, Instant releasedTime) {
        // 释放动作只允许在 RESERVED 后执行，并且原因由领域枚举约束，便于上游做补偿分流。
        ensureStatus(InventoryReservationStatus.RESERVED);
        this.reservationStatus = InventoryReservationStatus.RELEASED;
        this.releaseReason = reason;
        this.releasedAt = releasedTime;
    }

    public void deduct(Instant deductedTime) {
        // 扣减与释放互斥，只能在仍然持有预占量时完成，避免重复消费同一预占单。
        ensureStatus(InventoryReservationStatus.RESERVED);
        this.reservationStatus = InventoryReservationStatus.DEDUCTED;
        this.deductedAt = deductedTime;
    }

    private void ensureStatus(InventoryReservationStatus... expectedStatuses) {
        // 预占单状态机不做静默幂等，非法重复调用由上层显式识别并决定是否审计或短路。
        for (InventoryReservationStatus expectedStatus : expectedStatuses) {
            if (expectedStatus.equals(reservationStatus)) {
                return;
            }
        }
        throw new InventoryDomainException(
                InventoryErrorCode.INVALID_RESERVATION_STATUS,
                reservationStatus == null ? null : reservationStatus.value());
    }
}
