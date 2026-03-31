package com.github.thundax.bacon.upms.infra.persistence.dataobject;

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
@TableName("bacon_upms_data_permission_rule")
public class DataPermissionRuleDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private TenantId tenantId;
    @TableField("role_id")
    private Long roleId;
    @TableField("data_scope_type")
    private String dataScopeType;
    @TableField("created_by")
    private String createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_by")
    private String updatedBy;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
