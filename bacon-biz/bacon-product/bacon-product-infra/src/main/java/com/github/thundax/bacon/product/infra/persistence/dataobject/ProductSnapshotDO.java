package com.github.thundax.bacon.product.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_product_snapshot")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class ProductSnapshotDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("order_no")
    private String orderNo;

    @TableField("order_item_no")
    private String orderItemNo;

    @TableField("spu_id")
    private Long spuId;

    @TableField("spu_code")
    private String spuCode;

    @TableField("spu_name")
    private String spuName;

    @TableField("sku_id")
    private Long skuId;

    @TableField("sku_code")
    private String skuCode;

    @TableField("sku_name")
    private String skuName;

    @TableField("category_id")
    private Long categoryId;

    @TableField("category_name")
    private String categoryName;

    @TableField("spec_attributes")
    private String specAttributes;

    @TableField("sale_price")
    private BigDecimal salePrice;

    @TableField("quantity")
    private Integer quantity;

    @TableField("main_image_object_id")
    private String mainImageObjectId;

    @TableField("product_version")
    private Long productVersion;

    @TableField("created_at")
    private Instant createdAt;
}
