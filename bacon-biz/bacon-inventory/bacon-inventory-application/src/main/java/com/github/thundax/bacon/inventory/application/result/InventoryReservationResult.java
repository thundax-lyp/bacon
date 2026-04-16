package com.github.thundax.bacon.inventory.application.result;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占应用结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationResult {

    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 预占状态。 */
    private String reservationStatus;
    /** 库存状态。 */
    private String inventoryStatus;
    /** 仓库业务编码。 */
    private String warehouseCode;
    /** 失败原因。 */
    private String failureReason;
    /** 释放原因。 */
    private String releaseReason;
    /** 释放时间。 */
    private Instant releasedAt;
    /** 扣减时间。 */
    private Instant deductedAt;
}
