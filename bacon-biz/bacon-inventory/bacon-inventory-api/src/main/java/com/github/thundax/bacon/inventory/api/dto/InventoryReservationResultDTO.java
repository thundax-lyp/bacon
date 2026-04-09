package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 库存预占结果传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationResultDTO {

    /** 所属租户主键。 */
    private Long tenantId;
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
