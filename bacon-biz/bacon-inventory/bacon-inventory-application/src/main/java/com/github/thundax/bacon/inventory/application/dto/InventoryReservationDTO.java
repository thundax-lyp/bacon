package com.github.thundax.bacon.inventory.application.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占详情传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationDTO {

    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 预占状态。 */
    private String reservationStatus;
    /** 仓库业务编码。 */
    private String warehouseCode;
    /** 预占明细列表。 */
    private List<InventoryReservationItemDTO> items;
    /** 失败原因。 */
    private String failureReason;
    /** 释放原因。 */
    private String releaseReason;
    /** 创建时间。 */
    private Instant createdAt;
    /** 释放时间。 */
    private Instant releasedAt;
    /** 扣减时间。 */
    private Instant deductedAt;
}
