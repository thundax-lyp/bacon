package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import java.util.ArrayList;

public final class DepartmentAssembler {

    private DepartmentAssembler() {}

    public static DepartmentDTO toDto(Department department) {
        return new DepartmentDTO(
                DepartmentIdCodec.toValue(department.getId()),
                department.getCode(),
                department.getName(),
                DepartmentIdCodec.toValue(department.getParentId()),
                UserIdCodec.toValue(department.getLeaderUserId()),
                department.getSort(),
                toStatusValue(department.getStatus()));
    }

    public static DepartmentTreeDTO toTreeDto(Department department) {
        return new DepartmentTreeDTO(
                DepartmentIdCodec.toValue(department.getId()),
                department.getCode(),
                department.getName(),
                DepartmentIdCodec.toValue(department.getParentId()),
                UserIdCodec.toValue(department.getLeaderUserId()),
                department.getSort(),
                toStatusValue(department.getStatus()),
                new ArrayList<>());
    }

    private static String toStatusValue(DepartmentStatus status) {
        return status == null ? null : status.value();
    }
}
