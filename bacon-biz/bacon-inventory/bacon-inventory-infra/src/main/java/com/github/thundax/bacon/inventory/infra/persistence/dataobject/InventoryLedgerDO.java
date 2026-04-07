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
@TableName("bacon_inventory_ledger")
public class InventoryLedgerDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("order_no")
    private String orderNo;
    @TableField("reservation_no")
    private String reservationNo;
    @TableField("sku_id")
    private Long skuId;
    @TableField("warehouse_no")
    private String warehouseNo;
    @TableField("ledger_type")
    private String ledgerType;
    private Integer quantity;
    @TableField("occurred_at")
    private Instant occurredAt;
}
