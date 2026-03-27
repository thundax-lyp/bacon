package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 系统日志分页查询对象。
 */
public class SysLogQueryDTO {

    /** 租户标识。 */
    private String tenantId;
    /** 业务模块。 */
    private String module;
    /** 事件类型。 */
    private String eventType;
    /** 执行结果。 */
    private String result;
    /** 操作人名称。 */
    private String operatorName;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
