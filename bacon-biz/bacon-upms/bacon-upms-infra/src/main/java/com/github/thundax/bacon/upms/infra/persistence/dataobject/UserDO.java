package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.UserId;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_user")
public class UserDO {

    @TableId(type = IdType.INPUT)
    private UserId id;
    @TableField("tenant_id")
    private TenantId tenantId;
    @TableField("name")
    private String name;
    @TableField("avatar_object_id")
    private StoredObjectId avatarObjectId;
    @TableField("department_id")
    private DepartmentId departmentId;
    @TableField("status")
    private String status;
    @TableField("deleted")
    private Boolean deleted;
    @TableField("created_by")
    private String createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_by")
    private String updatedBy;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
