package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
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
    /** 头像存储对象编号。 */
    private AvatarStoredObjectNo avatarStoredObjectNo;
    /** 所属部门主键。 */
    private DepartmentId departmentId;
    /** 用户状态。 */
    private UserStatus status;

    public static User create(
            UserId id,
            String name,
            AvatarStoredObjectNo avatarStoredObjectNo,
            DepartmentId departmentId,
            UserStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new User(id, name, avatarStoredObjectNo, departmentId, status);
    }

    public static User reconstruct(
            UserId id,
            String name,
            AvatarStoredObjectNo avatarStoredObjectNo,
            DepartmentId departmentId,
            UserStatus status) {
        return new User(id, name, avatarStoredObjectNo, departmentId, status);
    }

    public User update(
            String name, AvatarStoredObjectNo avatarStoredObjectNo, DepartmentId departmentId, UserStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new User(id, name, avatarStoredObjectNo, departmentId, status);
    }
}
