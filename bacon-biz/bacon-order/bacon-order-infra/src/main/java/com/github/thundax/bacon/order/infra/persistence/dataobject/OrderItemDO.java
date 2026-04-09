package com.github.thundax.bacon.order.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_order_item")
public class OrderItemDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("order_id")
    private String orderId;

    @TableField("sku_id")
    private String skuId;

    @TableField("sku_name")
    private String skuName;

    @TableField("image_url")
    private String imageUrl;

    @TableField("quantity")
    private Integer quantity;

    @TableField("sale_price")
    private BigDecimal salePrice;

    @TableField("line_amount")
    private BigDecimal lineAmount;
}
