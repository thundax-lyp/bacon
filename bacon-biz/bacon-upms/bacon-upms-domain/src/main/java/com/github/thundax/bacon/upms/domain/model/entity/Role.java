package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
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
    private String code;
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
            String code,
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
            String code,
            String name,
            RoleType roleType,
            RoleDataScopeType dataScopeType,
            RoleStatus status) {
        return new Role(id, code, name, roleType, dataScopeType, status);
    }

    public Role update(
            String code, String name, RoleType roleType, RoleDataScopeType dataScopeType, RoleStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(roleType, "roleType must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Role(id, code, name, roleType, dataScopeType, status);
    }
}
