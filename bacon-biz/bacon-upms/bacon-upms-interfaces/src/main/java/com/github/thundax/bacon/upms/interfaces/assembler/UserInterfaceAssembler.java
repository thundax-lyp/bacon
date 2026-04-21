package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.application.command.UserAvatarUpdateCommand;
import com.github.thundax.bacon.upms.application.command.UserCreateCommand;
import com.github.thundax.bacon.upms.application.command.UserPasswordChangeCommand;
import com.github.thundax.bacon.upms.application.command.UserPasswordResetCommand;
import com.github.thundax.bacon.upms.application.command.UserRoleAssignCommand;
import com.github.thundax.bacon.upms.application.command.UserStatusUpdateCommand;
import com.github.thundax.bacon.upms.application.command.UserUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.application.query.UserExportQuery;
import com.github.thundax.bacon.upms.application.query.UserIdentityQuery;
import com.github.thundax.bacon.upms.application.query.UserLoginCredentialQuery;
import com.github.thundax.bacon.upms.application.query.UserPageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.interfaces.request.UserCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserIdentityQueryRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserPageRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserPasswordResetRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserRoleAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserIdentityResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public final class UserInterfaceAssembler {

    private UserInterfaceAssembler() {}

    public static UserCreateCommand toCreateCommand(UserCreateRequest request) {
        return new UserCreateCommand(request.account(), request.name(), request.phone(), DepartmentId.of(request.departmentId()));
    }

    public static UserUpdateCommand toUpdateCommand(Long userId, UserUpdateRequest request) {
        return new UserUpdateCommand(
                UserId.of(userId),
                request.account(),
                request.name(),
                request.phone(),
                DepartmentId.of(request.departmentId()));
    }

    public static UserStatusUpdateCommand toStatusUpdateCommand(Long userId, UserStatusUpdateRequest request) {
        return new UserStatusUpdateCommand(
                UserId.of(userId), request.status() == null ? null : UserStatus.from(request.status()));
    }

    public static UserRoleAssignCommand toRoleAssignCommand(Long userId, UserRoleAssignRequest request) {
        return new UserRoleAssignCommand(
                UserId.of(userId),
                request.roleIds() == null ? List.of() : request.roleIds().stream().map(RoleId::of).toList());
    }

    public static UserPageQuery toPageQuery(UserPageRequest request) {
        return new UserPageQuery(
                request.getAccount(),
                request.getName(),
                request.getPhone(),
                request.getStatus() == null ? null : UserStatus.from(request.getStatus()),
                request.getPageNo(),
                request.getPageSize());
    }

    public static UserExportQuery toExportQuery(UserPageRequest request) {
        return new UserExportQuery(
                request.getAccount(),
                request.getName(),
                request.getPhone(),
                request.getStatus() == null ? null : UserStatus.from(request.getStatus()));
    }

    public static UserIdentityQuery toIdentityQuery(UserIdentityQueryRequest request) {
        return new UserIdentityQuery(UserIdentityType.from(request.getIdentityType()), request.getIdentityValue());
    }

    public static UserIdentityQuery toIdentityQuery(UserIdentityGetFacadeRequest request) {
        return new UserIdentityQuery(UserIdentityType.from(request.getIdentityType()), request.getIdentityValue());
    }

    public static UserLoginCredentialQuery toLoginCredentialQuery(UserCredentialGetFacadeRequest request) {
        return new UserLoginCredentialQuery(UserIdentityType.from(request.getIdentityType()), request.getIdentityValue());
    }

    public static UserPasswordResetCommand toPasswordResetCommand(Long userId, UserPasswordResetRequest request) {
        return new UserPasswordResetCommand(UserId.of(userId), request.newPassword());
    }

    public static UserPasswordChangeCommand toPasswordChangeCommand(
            UserId userId, UserPasswordChangeFacadeRequest request) {
        return new UserPasswordChangeCommand(userId, request.getOldPassword(), request.getNewPassword());
    }

    public static UserAvatarUpdateCommand toAvatarUpdateCommand(Long userId, MultipartFile file) throws IOException {
        return new UserAvatarUpdateCommand(
                UserId.of(userId),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream());
    }

    public static UserResponse toResponse(UserDTO dto) {
        return UserResponse.from(dto);
    }

    public static UserPageResponse toPageResponse(PageResult<UserDTO> dto) {
        return UserPageResponse.from(dto);
    }

    public static UserIdentityResponse toResponse(UserIdentityDTO dto) {
        return UserIdentityResponse.from(dto);
    }

    public static UserFacadeResponse toFacadeResponse(UserDTO dto) {
        return toFacadeResponse(dto, null);
    }

    public static UserFacadeResponse toFacadeResponse(UserDTO dto, String departmentCode) {
        return new UserFacadeResponse(
                dto.getId(),
                dto.getAccount(),
                dto.getName(),
                dto.getAvatarStoredObjectNo(),
                dto.getPhone(),
                departmentCode,
                dto.getAvatarUrl(),
                dto.getStatus());
    }

    public static UserIdentityFacadeResponse toIdentityFacadeResponse(UserIdentityDTO dto) {
        return new UserIdentityFacadeResponse(
                dto.getId(), dto.getUserId(), dto.getIdentityType(), dto.getIdentityValue(), dto.getStatus());
    }

    public static UserCredentialFacadeResponse toCredentialFacadeResponse(UserLoginCredentialDTO dto) {
        return new UserCredentialFacadeResponse(
                dto.getUserId(),
                dto.getIdentityId(),
                dto.getAccount(),
                dto.getPhone(),
                dto.getIdentityType(),
                dto.getIdentityValue(),
                dto.getIdentityStatus(),
                dto.getCredentialId(),
                dto.getCredentialType(),
                dto.getCredentialStatus(),
                dto.isNeedChangePassword(),
                dto.getCredentialExpiresAt(),
                dto.getLockedUntil(),
                dto.isMfaRequired(),
                dto.getSecondFactorTypes(),
                dto.getStatus(),
                dto.getPasswordHash());
    }

    public static List<UserResponse> toResponseList(List<UserDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(UserInterfaceAssembler::toResponse).toList();
    }

    public static List<RoleResponse> toRoleResponseList(List<RoleDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(RoleResponse::from).toList();
    }
}
