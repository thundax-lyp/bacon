package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.application.codec.DepartmentCodeCodec;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.command.DepartmentCreateCommand;
import com.github.thundax.bacon.upms.application.command.DepartmentSortUpdateCommand;
import com.github.thundax.bacon.upms.application.command.DepartmentUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentBatchQueryRequest;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentSortUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentResponse;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentTreeResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DepartmentInterfaceAssembler {

    private DepartmentInterfaceAssembler() {}

    public static DepartmentCreateCommand toCreateCommand(DepartmentCreateRequest request) {
        return new DepartmentCreateCommand(
                DepartmentCodeCodec.toDomain(request.code()),
                trimPreservingNull(request.name()),
                DepartmentIdCodec.toDomain(request.parentId()),
                request.leaderUserId() == null ? null : UserId.of(request.leaderUserId()));
    }

    public static DepartmentUpdateCommand toUpdateCommand(Long departmentId, DepartmentUpdateRequest request) {
        return new DepartmentUpdateCommand(
                DepartmentId.of(departmentId),
                DepartmentCodeCodec.toDomain(request.code()),
                trimPreservingNull(request.name()),
                DepartmentIdCodec.toDomain(request.parentId()),
                request.leaderUserId() == null ? null : UserId.of(request.leaderUserId()),
                request.sort());
    }

    public static DepartmentSortUpdateCommand toSortUpdateCommand(
            Long departmentId, DepartmentSortUpdateRequest request) {
        return new DepartmentSortUpdateCommand(DepartmentId.of(departmentId), request.sort());
    }

    public static DepartmentCode toDepartmentCode(String departmentCode) {
        return DepartmentCodeCodec.toDomain(departmentCode);
    }

    public static Set<DepartmentId> toDepartmentIds(DepartmentBatchQueryRequest request) {
        return request.getDepartmentIds() == null
                ? Set.of()
                : request.getDepartmentIds().stream().map(DepartmentId::of).collect(Collectors.toSet());
    }

    public static DepartmentResponse toResponse(DepartmentDTO dto) {
        return DepartmentResponse.from(dto);
    }

    public static List<DepartmentResponse> toResponseList(List<DepartmentDTO> dtos) {
        return dtos.stream().map(DepartmentResponse::from).toList();
    }

    public static List<DepartmentTreeResponse> toTreeResponseList(List<DepartmentTreeDTO> dtos) {
        return dtos.stream().map(DepartmentTreeResponse::from).toList();
    }

    private static String trimPreservingNull(String value) {
        return value == null ? null : value.trim();
    }
}
