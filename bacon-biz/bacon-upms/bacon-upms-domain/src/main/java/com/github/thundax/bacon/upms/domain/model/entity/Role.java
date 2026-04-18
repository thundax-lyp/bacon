package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.upms.domain.exception.RoleDomainException;
import com.github.thundax.bacon.upms.domain.exception.RoleErrorCode;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 角色领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Role {

    /** 角色主键。 */
    private RoleId id;
    /** 角色编码。 */
    private RoleCode code;
    /** 角色名称。 */
    private String name;
    /** 角色类型。 */
    private RoleType roleType;
    /** 数据范围类型。 */
    private RoleDataScopeType dataScopeType;
    /** 角色状态。 */
    private RoleStatus status;

    public static Role create(
            RoleId id,
            RoleCode code,
            String name,
            RoleType roleType,
            RoleDataScopeType dataScopeType,
            RoleStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(roleType, "roleType must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Role(id, code, name, roleType, dataScopeType, status);
    }

    public static Role reconstruct(
            RoleId id,
            RoleCode code,
            String name,
            RoleType roleType,
            RoleDataScopeType dataScopeType,
            RoleStatus status) {
        return new Role(id, code, name, roleType, dataScopeType, status);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new RoleDomainException(RoleErrorCode.INVALID_ROLE_NAME);
        }
        this.name = name.trim();
    }

    public void changeDataScope(RoleDataScopeType dataScopeType) {
        Objects.requireNonNull(dataScopeType, "dataScopeType must not be null");
        this.dataScopeType = dataScopeType;
    }

    public void changeCode(RoleCode code) {
        Objects.requireNonNull(code, "code must not be null");
        this.code = code;
    }

    public boolean isSystemRole() {
        return roleType == RoleType.SYSTEM_ROLE;
    }

    public void assertActive() {
        if (status != RoleStatus.ACTIVE) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_ACTIVE);
        }
    }

    public void activate() {
        this.status = RoleStatus.ACTIVE;
    }

    public void disable() {
        this.status = RoleStatus.DISABLED;
    }

    public boolean hasAllDataAccess() {
        return dataScopeType == RoleDataScopeType.ALL;
    }

    public boolean hasDepartmentDataAccess() {
        return dataScopeType == RoleDataScopeType.DEPARTMENT;
    }

    public boolean hasSelfDataAccess() {
        return dataScopeType == RoleDataScopeType.SELF;
    }
}
