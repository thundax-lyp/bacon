package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_post")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class PostDO {

    @TableId(type = IdType.INPUT)
    private PostId id;

    @TableField("tenant_id")
    private TenantId tenantId;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("department_id")
    private DepartmentId departmentId;

    @TableField("status")
    private String status;
}
