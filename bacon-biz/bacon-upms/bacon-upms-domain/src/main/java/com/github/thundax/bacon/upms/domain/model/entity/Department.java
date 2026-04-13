package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 部门领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Department {

    /** 部门主键。 */
    private DepartmentId id;
    /** 部门编码。 */
    private String code;
    /** 部门名称。 */
    private String name;
    /** 父部门主键，根部门固定为 0。 */
    private DepartmentId parentId;
    /** 部门负责人用户主键。 */
    private UserId leaderUserId;
    /** 排序值。 */
    private Integer sort;
    /** 部门状态。 */
    private DepartmentStatus status;

    public static Department create(
            DepartmentId id,
            String code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Department(id, code, name, parentId, leaderUserId, sort, status);
    }

    public static Department reconstruct(
            DepartmentId id,
            String code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status) {
        return new Department(id, code, name, parentId, leaderUserId, sort, status);
    }

    public Department update(
            String code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Department(id, code, name, parentId, leaderUserId, sort, status);
    }
}
