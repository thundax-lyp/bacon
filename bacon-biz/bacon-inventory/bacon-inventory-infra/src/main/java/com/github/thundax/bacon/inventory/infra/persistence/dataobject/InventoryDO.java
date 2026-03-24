package com.github.thundax.bacon.inventory.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_inventory_inventory")
public class InventoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("sku_id")
    private Long skuId;
    @TableField("warehouse_id")
    private Long warehouseId;
    @TableField("on_hand_quantity")
    private Integer onHandQuantity;
    @TableField("reserved_quantity")
    private Integer reservedQuantity;
    @TableField("available_quantity")
    private Integer availableQuantity;
    private String status;
    @Version
    private Long version;
    @TableField("created_by")
    private Long createdBy;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_by")
    private Long updatedBy;
    @TableField("updated_at")
    private Instant updatedAt;
}
