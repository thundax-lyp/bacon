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
@TableName("bacon_product_spu")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class ProductSpuDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("spu_code")
    private String spuCode;

    @TableField("spu_name")
    private String spuName;

    @TableField("category_id")
    private Long categoryId;

    @TableField("description")
    private String description;

    @TableField("main_image_object_id")
    private String mainImageObjectId;

    @TableField("product_status")
    private String productStatus;

    @TableField("version")
    private Long version;

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
