package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_department")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class DepartmentDO {

    @TableId(type = IdType.INPUT)
    private DepartmentId id;

    @TableField("tenant_id")
    private TenantId tenantId;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("parent_id")
    private DepartmentId parentId;

    @TableField("leader_user_id")
    private UserId leaderUserId;

    @TableField("sort")
    private Integer sort;

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
