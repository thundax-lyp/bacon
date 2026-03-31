package com.github.thundax.bacon.upms.infra.persistence.dataobject;

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
@TableName("bacon_upms_role")
public class RoleDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("code")
    private String code;
    @TableField("name")
    private String name;
    @TableField("role_type")
    private String roleType;
    @TableField("data_scope_type")
    private String dataScopeType;
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
