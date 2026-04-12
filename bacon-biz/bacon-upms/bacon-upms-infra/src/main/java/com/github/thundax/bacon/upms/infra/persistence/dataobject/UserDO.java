package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_user")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
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
}
