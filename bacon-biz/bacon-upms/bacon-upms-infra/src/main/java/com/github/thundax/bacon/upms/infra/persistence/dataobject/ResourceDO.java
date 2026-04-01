package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_resource")
public class ResourceDO {

    @TableId(type = IdType.INPUT)
    private ResourceId id;
    @TableField("tenant_id")
    private TenantId tenantId;
    @TableField("code")
    private String code;
    @TableField("name")
    private String name;
    @TableField("resource_type")
    private String resourceType;
    @TableField("method")
    private String httpMethod;
    @TableField("path")
    private String uri;
    @TableField("status")
    private String status;
    @TableField("created_by")
    private String createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_by")
    private String updatedBy;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
