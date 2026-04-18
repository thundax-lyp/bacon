package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.exception.DepartmentDomainException;
import com.github.thundax.bacon.upms.domain.exception.DepartmentErrorCode;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
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
    private DepartmentCode code;
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
            DepartmentId id, DepartmentCode code, String name, DepartmentId parentId, UserId leaderUserId) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        validateParentId(id, parentId);
        return new Department(id, code, name, parentId, leaderUserId, 0, DepartmentStatus.ENABLED);
    }

    public static Department reconstruct(
            DepartmentId id,
            DepartmentCode code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status) {
        return new Department(id, code, name, parentId, leaderUserId, sort, status);
    }

    public void update(DepartmentCode code, String name, DepartmentId parentId, UserId leaderUserId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        validateParentId(id, parentId);
        this.code = code;
        this.name = name;
        this.parentId = parentId;
        this.leaderUserId = leaderUserId;
    }

    public void sort(Integer sort) {
        if (sort == null || sort < 0) {
            throw new DepartmentDomainException(DepartmentErrorCode.INVALID_DEPARTMENT_SORT);
        }
        this.sort = sort;
    }

    public void enable() {
        this.status = DepartmentStatus.ENABLED;
    }

    public void disable() {
        this.status = DepartmentStatus.DISABLED;
    }

    public void changeCode(DepartmentCode code) {
        Objects.requireNonNull(code, "code must not be null");
        this.code = code;
    }

    private static void validateParentId(DepartmentId id, DepartmentId parentId) {
        if (Objects.equals(id, parentId)) {
            throw new DepartmentDomainException(DepartmentErrorCode.DEPARTMENT_PARENT_CANNOT_BE_SELF);
        }
    }
}
