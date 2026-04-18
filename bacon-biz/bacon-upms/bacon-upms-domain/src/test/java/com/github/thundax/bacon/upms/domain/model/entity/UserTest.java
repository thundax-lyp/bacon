package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldRenameUser() {
        User user = User.create(UserId.of(101L), "Alice", null, null, UserStatus.ACTIVE);

        user.rename("Alice New");

        assertThat(user.getNickname()).isEqualTo("Alice New");
    }

    @Test
    void shouldChangeAndRemoveAvatar() {
        User user = User.create(UserId.of(101L), "Alice", null, null, UserStatus.ACTIVE);

        user.useAvatar(AvatarStoredObjectNo.of("storage-20260327100000-000001"));
        assertThat(user.getAvatarStoredObjectNo()).isEqualTo(AvatarStoredObjectNo.of("storage-20260327100000-000001"));

        user.removeAvatar();
        assertThat(user.getAvatarStoredObjectNo()).isNull();
    }

    @Test
    void shouldChangeAndClearDepartment() {
        User user = User.create(UserId.of(101L), "Alice", null, null, UserStatus.ACTIVE);

        user.assignDepartment(DepartmentId.of(201L));
        assertThat(user.getDepartmentId()).isEqualTo(DepartmentId.of(201L));

        user.clearDepartment();
        assertThat(user.getDepartmentId()).isNull();
    }

    @Test
    void shouldReportAndAssertActiveStatus() {
        User activeUser = User.create(UserId.of(101L), "Alice", null, null, UserStatus.ACTIVE);
        User disabledUser = User.create(UserId.of(102L), "Bob", null, null, UserStatus.DISABLED);

        assertThat(activeUser.isActive()).isTrue();
        assertThat(disabledUser.isActive()).isFalse();
        assertThatCode(activeUser::assertActive).doesNotThrowAnyException();
        assertThatThrownBy(disabledUser::assertActive)
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User is not active");
    }
}
