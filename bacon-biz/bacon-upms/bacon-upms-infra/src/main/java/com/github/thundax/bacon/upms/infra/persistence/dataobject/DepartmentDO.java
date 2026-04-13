package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
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
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("parent_id")
    private Long parentId;

    @TableField("leader_user_id")
    private Long leaderUserId;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private String status;
}
