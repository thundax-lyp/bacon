package com.github.thundax.bacon.product.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_product_image")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class ProductImageDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("spu_id")
    private Long spuId;

    @TableField("sku_id")
    private Long skuId;

    @TableField("object_id")
    private String objectId;

    @TableField("image_type")
    private String imageType;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("deleted")
    private Boolean deleted;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_at")
    private Instant updatedAt;
}
