package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.common.core.result.PageResult;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.codec.ResourceCodeCodec;
import com.github.thundax.bacon.upms.application.codec.RoleCodeCodec;
import com.github.thundax.bacon.upms.application.codec.RoleIdCodec;
import com.github.thundax.bacon.upms.application.command.RoleCreateCommand;
import com.github.thundax.bacon.upms.application.command.RoleDataScopeAssignCommand;
import com.github.thundax.bacon.upms.application.command.RoleMenuAssignCommand;
import com.github.thundax.bacon.upms.application.command.RoleResourceAssignCommand;
import com.github.thundax.bacon.upms.application.command.RoleStatusUpdateCommand;
import com.github.thundax.bacon.upms.application.command.RoleUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.query.RolePageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.interfaces.request.RoleCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleDataScopeAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleMenuAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.RolePageRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleResourceAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleDataScopeResponse;
import com.github.thundax.bacon.upms.interfaces.response.RolePageResponse;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleInterfaceAssembler {

    private RoleInterfaceAssembler() {}

    public static RolePageQuery toPageQuery(RolePageRequest request) {
        return new RolePageQuery(
                RoleCodeCodec.toDomain(request.getCode()),
                request.getName(),
                request.getRoleType() == null ? null : RoleType.from(request.getRoleType()),
                request.getStatus() == null ? null : RoleStatus.from(request.getStatus()),
                request.getPageNo(),
                request.getPageSize());
    }

    public static RoleCreateCommand toCreateCommand(RoleCreateRequest request) {
        return new RoleCreateCommand(
                RoleCodeCodec.toDomain(request.code()),
                request.name(),
                request.roleType() == null ? null : RoleType.from(request.roleType()),
                request.dataScopeType() == null ? null : RoleDataScopeType.from(request.dataScopeType()));
    }

    public static RoleUpdateCommand toUpdateCommand(Long roleId, RoleUpdateRequest request) {
        return new RoleUpdateCommand(
                RoleIdCodec.toDomain(roleId),
                RoleCodeCodec.toDomain(request.code()),
                request.name(),
                request.roleType() == null ? null : RoleType.from(request.roleType()),
                request.dataScopeType() == null ? null : RoleDataScopeType.from(request.dataScopeType()));
    }

    public static RoleStatusUpdateCommand toStatusUpdateCommand(Long roleId, RoleStatusUpdateRequest request) {
        return new RoleStatusUpdateCommand(
                RoleIdCodec.toDomain(roleId), request.status() == null ? null : RoleStatus.from(request.status()));
    }

    public static RoleMenuAssignCommand toMenuAssignCommand(Long roleId, RoleMenuAssignRequest request) {
        return new RoleMenuAssignCommand(
                RoleIdCodec.toDomain(roleId),
                request.menuIds() == null
                        ? Set.of()
                        : request.menuIds().stream().map(MenuIdCodec::toDomain).collect(Collectors.toSet()));
    }

    public static RoleResourceAssignCommand toResourceAssignCommand(Long roleId, RoleResourceAssignRequest request) {
        Set<ResourceCode> resourceCodes = request.resourceCodes() == null
                ? Set.of()
                : request.resourceCodes().stream()
                        .map(ResourceCodeCodec::toDomain)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        return new RoleResourceAssignCommand(RoleIdCodec.toDomain(roleId), resourceCodes);
    }

    public static RoleDataScopeAssignCommand toDataScopeAssignCommand(Long roleId, RoleDataScopeAssignRequest request) {
        return new RoleDataScopeAssignCommand(
                RoleIdCodec.toDomain(roleId),
                request.dataScopeType() == null ? null : RoleDataScopeType.from(request.dataScopeType()),
                request.departmentIds() == null
                        ? Set.<DepartmentId>of()
                        : request.departmentIds().stream().map(DepartmentIdCodec::toDomain).collect(Collectors.toSet()));
    }

    public static RoleResponse toResponse(RoleDTO dto) {
        return RoleResponse.from(dto);
    }

    public static RolePageResponse toPageResponse(PageResult<RoleDTO> pageResult) {
        return RolePageResponse.from(pageResult);
    }

    public static RoleDataScopeResponse toDataScopeResponse(String dataScopeType, Set<DepartmentId> departmentIds) {
        return new RoleDataScopeResponse(
                dataScopeType,
                departmentIds.stream().map(DepartmentId::value).collect(Collectors.toSet()));
    }

    public static RoleId toRoleId(Long roleId) {
        return RoleIdCodec.toDomain(roleId);
    }

    public static Set<Long> toDepartmentIdValues(Set<DepartmentId> departmentIds) {
        return departmentIds.stream().map(DepartmentId::value).collect(Collectors.toSet());
    }
}
