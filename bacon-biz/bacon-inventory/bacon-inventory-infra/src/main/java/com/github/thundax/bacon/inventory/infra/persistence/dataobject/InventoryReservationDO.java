package com.github.thundax.bacon.inventory.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_inventory_reservation")
public class InventoryReservationDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("reservation_no")
    private String reservationNo;
    @TableField("order_no")
    private String orderNo;
    @TableField("reservation_status")
    private String reservationStatus;
    @TableField("warehouse_no")
    private String warehouseNo;
    @TableField("failure_reason")
    private String failureReason;
    @TableField("release_reason")
    private String releaseReason;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("released_at")
    private Instant releasedAt;
    @TableField("deducted_at")
    private Instant deductedAt;
}
