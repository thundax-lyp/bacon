package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_resource")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
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
}
