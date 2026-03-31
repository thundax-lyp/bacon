package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.TenantId;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_tenant")
public class TenantDO {

    @TableId(value = "tenant_id", type = IdType.INPUT)
    private TenantId id;
    @TableField("name")
    private String name;
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
