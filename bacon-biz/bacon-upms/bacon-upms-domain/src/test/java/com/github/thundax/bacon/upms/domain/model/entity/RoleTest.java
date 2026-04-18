package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void shouldRenameRoleUsingTrimmedName() {
        Role role = Role.create(
                RoleId.of(101L), RoleCode.of("ADMIN"), "Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.ALL);

        role.rename("  Platform Admin  ");

        assertThat(role.getName()).isEqualTo("Platform Admin");
    }

    @Test
    void shouldRejectBlankRoleName() {
        Role role = Role.create(
                RoleId.of(101L), RoleCode.of("ADMIN"), "Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.ALL);

        assertThatThrownBy(() -> role.rename("   "))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Role name must not be blank");
    }

    @Test
    void shouldChangeRoleScopeAndReportPredicates() {
        Role role = Role.create(
                RoleId.of(101L), RoleCode.of("ADMIN"), "Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.ALL);

        assertThat(role.isSystemRole()).isTrue();
        assertThat(role.hasAllDataAccess()).isTrue();
        assertThat(role.getStatus()).isEqualTo(RoleStatus.ACTIVE);

        role.assignDataScope(RoleDataScopeType.DEPARTMENT, Set.of());
        assertThat(role.hasDepartmentDataAccess()).isTrue();
        assertThat(role.hasAllDataAccess()).isFalse();

        role.assignDataScope(RoleDataScopeType.SELF, Set.of());
        assertThat(role.hasSelfDataAccess()).isTrue();
    }

    @Test
    void shouldAssignCustomDataScopeWithDepartments() {
        Role role = Role.create(
                RoleId.of(101L), RoleCode.of("ADMIN"), "Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.ALL);

        Set<DepartmentId> assignedDepartmentIds =
                role.assignDataScope(RoleDataScopeType.CUSTOM, Set.of(DepartmentId.of(11L), DepartmentId.of(12L)));

        assertThat(role.getDataScopeType()).isEqualTo(RoleDataScopeType.CUSTOM);
        assertThat(assignedDepartmentIds).containsExactlyInAnyOrder(DepartmentId.of(11L), DepartmentId.of(12L));
    }

    @Test
    void shouldClearDepartmentsForNonCustomDataScope() {
        Role role = Role.create(
                RoleId.of(101L), RoleCode.of("ADMIN"), "Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.CUSTOM);

        Set<DepartmentId> assignedDepartmentIds = role.assignDataScope(RoleDataScopeType.ALL, Set.of(DepartmentId.of(11L)));

        assertThat(role.getDataScopeType()).isEqualTo(RoleDataScopeType.ALL);
        assertThat(assignedDepartmentIds).isEmpty();
    }

    @Test
    void shouldRejectEmptyDepartmentsForCustomDataScope() {
        Role role = Role.create(
                RoleId.of(101L), RoleCode.of("ADMIN"), "Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.ALL);

        assertThatThrownBy(() -> role.assignDataScope(RoleDataScopeType.CUSTOM, Set.of()))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Custom role data scope departments must not be empty");
    }

    @Test
    void shouldToggleAndAssertRoleStatus() {
        Role role = Role.reconstruct(
                RoleId.of(101L),
                RoleCode.of("ADMIN"),
                "Admin",
                RoleType.CUSTOM_ROLE,
                RoleDataScopeType.SELF,
                RoleStatus.DISABLED);

        assertThatThrownBy(role::assertActive)
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Role is not active");

        role.activate();
        assertThatCode(role::assertActive).doesNotThrowAnyException();

        role.disable();
        assertThat(role.getStatus()).isEqualTo(RoleStatus.DISABLED);
    }

    @Test
    void shouldChangeRoleCode() {
        Role role = Role.create(
                RoleId.of(101L), RoleCode.of("ADMIN"), "Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.ALL);

        role.recodeAs(RoleCode.of("PLATFORM_ADMIN"));

        assertThat(role.getCode()).isEqualTo(RoleCode.of("PLATFORM_ADMIN"));
    }
}
