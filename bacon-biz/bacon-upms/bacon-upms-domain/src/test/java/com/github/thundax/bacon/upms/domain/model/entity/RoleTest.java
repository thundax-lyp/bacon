package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.upms.domain.exception.RoleDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void shouldRenameRoleUsingTrimmedName() {
        Role role = Role.create(
                RoleId.of(101L),
                RoleCode.of("ADMIN"),
                "Admin",
                RoleType.SYSTEM_ROLE,
                RoleDataScopeType.ALL,
                RoleStatus.ACTIVE);

        role.rename("  Platform Admin  ");

        assertThat(role.getName()).isEqualTo("Platform Admin");
    }

    @Test
    void shouldRejectBlankRoleName() {
        Role role = Role.create(
                RoleId.of(101L),
                RoleCode.of("ADMIN"),
                "Admin",
                RoleType.SYSTEM_ROLE,
                RoleDataScopeType.ALL,
                RoleStatus.ACTIVE);

        assertThatThrownBy(() -> role.rename("   "))
                .isInstanceOf(RoleDomainException.class)
                .hasMessage("Role name must not be blank");
    }

    @Test
    void shouldChangeRoleScopeAndReportPredicates() {
        Role role = Role.create(
                RoleId.of(101L),
                RoleCode.of("ADMIN"),
                "Admin",
                RoleType.SYSTEM_ROLE,
                RoleDataScopeType.ALL,
                RoleStatus.ACTIVE);

        assertThat(role.isSystemRole()).isTrue();
        assertThat(role.hasAllDataAccess()).isTrue();

        role.changeDataScope(RoleDataScopeType.DEPARTMENT);
        assertThat(role.hasDepartmentDataAccess()).isTrue();
        assertThat(role.hasAllDataAccess()).isFalse();

        role.changeDataScope(RoleDataScopeType.SELF);
        assertThat(role.hasSelfDataAccess()).isTrue();
    }

    @Test
    void shouldToggleAndAssertRoleStatus() {
        Role role = Role.create(
                RoleId.of(101L),
                RoleCode.of("ADMIN"),
                "Admin",
                RoleType.CUSTOM_ROLE,
                RoleDataScopeType.SELF,
                RoleStatus.DISABLED);

        assertThatThrownBy(role::assertActive)
                .isInstanceOf(RoleDomainException.class)
                .hasMessage("Role is not active");

        role.activate();
        assertThatCode(role::assertActive).doesNotThrowAnyException();

        role.disable();
        assertThat(role.getStatus()).isEqualTo(RoleStatus.DISABLED);
    }

    @Test
    void shouldChangeRoleCode() {
        Role role = Role.create(
                RoleId.of(101L),
                RoleCode.of("ADMIN"),
                "Admin",
                RoleType.SYSTEM_ROLE,
                RoleDataScopeType.ALL,
                RoleStatus.ACTIVE);

        role.changeCode(RoleCode.of("PLATFORM_ADMIN"));

        assertThat(role.getCode()).isEqualTo(RoleCode.of("PLATFORM_ADMIN"));
    }
}
