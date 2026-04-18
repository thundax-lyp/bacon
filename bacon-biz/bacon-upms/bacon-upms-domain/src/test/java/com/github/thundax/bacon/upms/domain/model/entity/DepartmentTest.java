package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.upms.domain.exception.DepartmentDomainException;
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
                .isInstanceOf(DepartmentDomainException.class)
                .hasMessage("Department parent cannot be self");
    }

    @Test
    void shouldRejectSelfAsParentWhenUpdatingDepartment() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);
        department.sort(1);

        assertThatThrownBy(() -> department.update(
                        DepartmentCode.of("OPS"),
                        "Operations",
                        DepartmentId.of(101L),
                        null))
                .isInstanceOf(DepartmentDomainException.class)
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
                .isInstanceOf(DepartmentDomainException.class)
                .hasMessage("Department sort must be greater than or equal to 0");
    }

    @Test
    void shouldChangeDepartmentCode() {
        Department department =
                Department.create(DepartmentId.of(101L), DepartmentCode.of("OPS"), "Operations", null, null);

        department.changeCode(DepartmentCode.of("OPS_NEW"));

        org.assertj.core.api.Assertions.assertThat(department.getCode()).isEqualTo(DepartmentCode.of("OPS_NEW"));
    }
}
