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
@TableName("bacon_product_archive")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class ProductArchiveDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("spu_id")
    private Long spuId;

    @TableField("product_version")
    private Long productVersion;

    @TableField("archive_type")
    private String archiveType;

    @TableField("archive_content")
    private String archiveContent;

    @TableField("created_at")
    private Instant createdAt;
}
