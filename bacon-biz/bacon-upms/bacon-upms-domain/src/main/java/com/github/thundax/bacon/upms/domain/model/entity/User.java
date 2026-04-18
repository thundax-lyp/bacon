package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.exception.UserDomainException;
import com.github.thundax.bacon.upms.domain.exception.UserErrorCode;
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
    /** 用户昵称。 */
    private String nickname;
    /** 头像存储对象编号。 */
    private AvatarStoredObjectNo avatarStoredObjectNo;
    /** 所属部门主键。 */
    private DepartmentId departmentId;
    /** 用户状态。 */
    private UserStatus status;

    public static User create(
            UserId id,
            String nickname,
            AvatarStoredObjectNo avatarStoredObjectNo,
            DepartmentId departmentId,
            UserStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(nickname, "nickname must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new User(id, nickname, avatarStoredObjectNo, departmentId, status);
    }

    public static User reconstruct(
            UserId id,
            String nickname,
            AvatarStoredObjectNo avatarStoredObjectNo,
            DepartmentId departmentId,
            UserStatus status) {
        return new User(id, nickname, avatarStoredObjectNo, departmentId, status);
    }

    public void rename(String nickname) {
        Objects.requireNonNull(nickname, "nickname must not be null");
        this.nickname = nickname;
    }

    public void changeAvatar(AvatarStoredObjectNo avatarStoredObjectNo) {
        Objects.requireNonNull(avatarStoredObjectNo, "avatarStoredObjectNo must not be null");
        this.avatarStoredObjectNo = avatarStoredObjectNo;
    }

    public void removeAvatar() {
        this.avatarStoredObjectNo = null;
    }

    public void changeDepartment(DepartmentId departmentId) {
        Objects.requireNonNull(departmentId, "departmentId must not be null");
        this.departmentId = departmentId;
    }

    public void clearDepartment() {
        this.departmentId = null;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public void assertActive() {
        if (!isActive()) {
            throw new UserDomainException(UserErrorCode.USER_NOT_ACTIVE);
        }
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
