package com.github.thundax.bacon.order.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_order_inventory_snapshot")
public class OrderInventorySnapshotDO {

    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("tenant_id")
    private String tenantId;
    @TableField("order_no")
    private String orderNo;
    @TableField("reservation_no")
    private String reservationNo;
    @TableField("inventory_status")
    private String inventoryStatus;
    @TableField("warehouse_no")
    private String warehouseNo;
    @TableField("failure_reason")
    private String failureReason;
    @TableField("updated_at")
    private Instant updatedAt;
}
