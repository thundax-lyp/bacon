package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_role_data_scope_rel")
public class RoleDataScopeRelDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private TenantId tenantId;
    @TableField("role_id")
    private RoleId roleId;
    @TableField("department_id")
    private DepartmentId departmentId;
}
