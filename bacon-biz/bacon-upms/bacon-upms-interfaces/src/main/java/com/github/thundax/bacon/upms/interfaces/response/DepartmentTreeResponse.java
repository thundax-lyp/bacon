package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.api.enums.EnableStatusEnum;
import java.util.List;

/**
 * 部门树查询响应对象。
 */
public record DepartmentTreeResponse(
        /** 部门主键。 */
        String id,
        /** 所属租户编号。 */
        Long tenantId,
        /** 部门编码。 */
        String code,
        /** 部门名称。 */
        String name,
        /** 父部门主键，根部门固定为 0。 */
        String parentId,
        /** 部门负责人用户主键。 */
        Long leaderUserId,
        /** 排序值。 */
        Integer sort,
        /** 部门状态。 */
        EnableStatusEnum status,
        /** 子部门列表。 */
        List<DepartmentTreeResponse> children) {

    public static DepartmentTreeResponse from(DepartmentTreeDTO dto) {
        List<DepartmentTreeResponse> childResponses = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(DepartmentTreeResponse::from).toList();
        return new DepartmentTreeResponse(idValue(dto.getId()), dto.getTenantId(), dto.getCode(), dto.getName(),
                idValue(dto.getParentId()), dto.getLeaderUserId(), dto.getSort(), dto.getStatus(), childResponses);
    }

    private static String idValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
