package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.DepartmentTreeDTO;
import java.util.List;

/**
 * 部门树查询响应对象。
 */
public record DepartmentTreeResponse(
        /** 部门主键。 */
        Long id,
        /** 部门编码。 */
        String code,
        /** 部门名称。 */
        String name,
        /** 父部门主键，根部门固定为 0。 */
        Long parentId,
        /** 部门负责人用户主键。 */
        Long leaderUserId,
        /** 排序值。 */
        Integer sort,
        /** 部门状态，可选值：ENABLED、DISABLED。 */
        String status,
        /** 子部门列表。 */
        List<DepartmentTreeResponse> children) {

    public static DepartmentTreeResponse from(DepartmentTreeDTO dto) {
        List<DepartmentTreeResponse> childResponses = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(DepartmentTreeResponse::from).toList();
        return new DepartmentTreeResponse(
                dto.getId(),
                dto.getCode(),
                dto.getName(),
                dto.getParentId(),
                dto.getLeaderUserId(),
                dto.getSort(),
                dto.getStatus(),
                childResponses);
    }
}
