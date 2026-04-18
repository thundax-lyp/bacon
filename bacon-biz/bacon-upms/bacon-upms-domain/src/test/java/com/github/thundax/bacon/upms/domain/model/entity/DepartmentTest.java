package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import org.junit.jupiter.api.Test;

class DepartmentTest {

    @Test
    void shouldRejectSelfAsParentWhenCreatingDepartment() {
        assertThatThrownBy(() -> Department.create(
                        DepartmentId.of(101L),
                        DepartmentCode.of("OPS"),
                        "Operations",
                        DepartmentId.of(101L),
                        null))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Department parent cannot be self");
    }

    @Test
    void shouldRejectSelfAsParentWhenUpdatingDepartment() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);
        department.sort(1);

        assertThatThrownBy(() -> department.moveUnder(DepartmentId.of(101L)))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Department parent cannot be self");
    }

    @Test
    void shouldToggleDepartmentStatus() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);

        department.disable();
        org.assertj.core.api.Assertions.assertThat(department.getStatus()).isEqualTo(DepartmentStatus.DISABLED);

        department.enable();
        org.assertj.core.api.Assertions.assertThat(department.getStatus()).isEqualTo(DepartmentStatus.ENABLED);
    }

    @Test
    void shouldDefaultSortAndStatusWhenCreatingDepartment() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);

        org.assertj.core.api.Assertions.assertThat(department.getSort()).isEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(department.getStatus()).isEqualTo(DepartmentStatus.ENABLED);
    }

    @Test
    void shouldRejectNegativeSortWhenSortingDepartment() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);

        assertThatThrownBy(() -> department.sort(-1))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Department sort must be greater than or equal to 0");
    }

    @Test
    void shouldChangeDepartmentCode() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);

        department.recodeAs(DepartmentCode.of("OPS_NEW"));

        org.assertj.core.api.Assertions.assertThat(department.getCode()).isEqualTo(DepartmentCode.of("OPS_NEW"));
    }

    @Test
    void shouldChangeDepartmentFields() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);

        department.rename("Operations New");
        department.moveUnder(DepartmentId.of(102L));
        department.appointLeader(com.github.thundax.bacon.common.id.domain.UserId.of(201L));

        org.assertj.core.api.Assertions.assertThat(department.getName()).isEqualTo("Operations New");
        org.assertj.core.api.Assertions.assertThat(department.getParentId()).isEqualTo(DepartmentId.of(102L));
        org.assertj.core.api.Assertions.assertThat(department.getLeaderUserId())
                .isEqualTo(com.github.thundax.bacon.common.id.domain.UserId.of(201L));
    }
}
