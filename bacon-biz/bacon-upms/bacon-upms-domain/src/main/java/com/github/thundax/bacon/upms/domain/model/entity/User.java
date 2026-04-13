package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    /** 用户主键。 */
    private UserId id;
    /** 用户名称。 */
    private String name;
    /** 头像对象主键。 */
    private StoredObjectId avatarObjectId;
    /** 所属部门主键。 */
    private DepartmentId departmentId;
    /** 用户状态。 */
    private UserStatus status;

    public static User create(
            UserId id, String name, StoredObjectId avatarObjectId, DepartmentId departmentId, UserStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new User(id, name, avatarObjectId, departmentId, status);
    }

    public static User reconstruct(
            UserId id, String name, StoredObjectId avatarObjectId, DepartmentId departmentId, UserStatus status) {
        return new User(id, name, avatarObjectId, departmentId, status);
    }

    public User update(String name, StoredObjectId avatarObjectId, DepartmentId departmentId, UserStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new User(id, name, avatarObjectId, departmentId, status);
    }
}
