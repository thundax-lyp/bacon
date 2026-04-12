package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.PostDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.SysLogRecordDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.TenantDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

abstract class AbstractUpmsPersistenceSupport {

    protected final String limit(int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNo - 1) * safePageSize;
        return "limit " + offset + "," + safePageSize;
    }

    protected final boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    protected final String trim(String value) {
        return value == null ? null : value.trim();
    }

    protected final LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    protected final Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    protected final TenantDO toDataObject(Tenant tenant) {
        return new TenantDO(
                tenant.getId(),
                tenant.getTenantCode().value(),
                tenant.getName(),
                tenant.getStatus().value(),
                toLocalDateTime(tenant.getExpiredAt()),
                tenant.getCreatedBy(),
                toLocalDateTime(tenant.getCreatedAt()),
                tenant.getUpdatedBy(),
                toLocalDateTime(tenant.getUpdatedAt()));
    }

    protected final Tenant toDomain(TenantDO tenantDO) {
        return Tenant.reconstruct(
                tenantDO.getId(),
                tenantDO.getName(),
                com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode.of(tenantDO.getCode()),
                TenantStatus.from(tenantDO.getStatus()),
                toInstant(tenantDO.getExpiredAt()),
                tenantDO.getCreatedBy(),
                toInstant(tenantDO.getCreatedAt()),
                tenantDO.getUpdatedBy(),
                toInstant(tenantDO.getUpdatedAt()));
    }

    protected final UserDO toDataObject(User user) {
        return new UserDO(
                user.getId(),
                user.getTenantId(),
                user.getName(),
                user.getAvatarObjectId(),
                user.getDepartmentId(),
                user.getStatus().value(),
                false,
                user.getCreatedBy(),
                toLocalDateTime(user.getCreatedAt()),
                user.getUpdatedBy(),
                toLocalDateTime(user.getUpdatedAt()));
    }

    protected final User toDomain(UserDO userDO) {
        return User.reconstruct(
                userDO.getId(),
                userDO.getTenantId(),
                userDO.getName(),
                userDO.getAvatarObjectId(),
                userDO.getDepartmentId(),
                UserStatus.valueOf(userDO.getStatus()),
                userDO.getCreatedBy(),
                toInstant(userDO.getCreatedAt()),
                userDO.getUpdatedBy(),
                toInstant(userDO.getUpdatedAt()));
    }

    protected final UserIdentityDO toDataObject(UserIdentity userIdentity) {
        return new UserIdentityDO(
                userIdentity.getId(),
                userIdentity.getTenantId(),
                userIdentity.getUserId(),
                userIdentity.getIdentityType() == null
                        ? null
                        : userIdentity.getIdentityType().value(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus() == null
                        ? null
                        : userIdentity.getStatus().value(),
                userIdentity.getCreatedBy(),
                toLocalDateTime(userIdentity.getCreatedAt()),
                userIdentity.getUpdatedBy(),
                toLocalDateTime(userIdentity.getUpdatedAt()));
    }

    protected final UserIdentity toDomain(UserIdentityDO dataObject) {
        return UserIdentity.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getUserId(),
                UserIdentityType.from(dataObject.getIdentityType()),
                dataObject.getIdentityValue(),
                UserIdentityStatus.from(dataObject.getStatus()),
                dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final UserCredentialDO toDataObject(UserCredential userCredential) {
        return new UserCredentialDO(
                userCredential.getId(),
                userCredential.getTenantId(),
                userCredential.getUserId(),
                userCredential.getIdentityId(),
                userCredential.getCredentialType() == null
                        ? null
                        : userCredential.getCredentialType().value(),
                userCredential.getFactorLevel() == null
                        ? null
                        : userCredential.getFactorLevel().value(),
                userCredential.getCredentialValue(),
                userCredential.getStatus().value(),
                userCredential.isNeedChangePassword(),
                userCredential.getFailedCount(),
                userCredential.getFailedLimit(),
                userCredential.getLockReason(),
                toLocalDateTime(userCredential.getLockedUntil()),
                toLocalDateTime(userCredential.getExpiresAt()),
                toLocalDateTime(userCredential.getLastVerifiedAt()),
                userCredential.getCreatedBy(),
                toLocalDateTime(userCredential.getCreatedAt()),
                userCredential.getUpdatedBy(),
                toLocalDateTime(userCredential.getUpdatedAt()));
    }

    protected final UserCredential toDomain(UserCredentialDO dataObject) {
        return UserCredential.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getUserId(),
                dataObject.getIdentityId(),
                UserCredentialType.from(dataObject.getCredentialType()),
                UserCredentialFactorLevel.from(dataObject.getFactorLevel()),
                dataObject.getCredentialValue(),
                UserCredentialStatus.from(dataObject.getStatus()),
                Boolean.TRUE.equals(dataObject.getNeedChangePassword()),
                dataObject.getFailedCount() == null ? 0 : dataObject.getFailedCount(),
                dataObject.getFailedLimit() == null ? 0 : dataObject.getFailedLimit(),
                dataObject.getLockReason(),
                toInstant(dataObject.getLockedUntil()),
                toInstant(dataObject.getExpiresAt()),
                toInstant(dataObject.getLastVerifiedAt()),
                dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final DepartmentDO toDataObject(Department department) {
        return new DepartmentDO(
                department.getId(),
                department.getTenantId(),
                department.getCode(),
                department.getName(),
                department.getParentId(),
                department.getLeaderUserId(),
                department.getSort(),
                department.getStatus() == null ? null : department.getStatus().value(),
                department.getCreatedBy(),
                toLocalDateTime(department.getCreatedAt()),
                department.getUpdatedBy(),
                toLocalDateTime(department.getUpdatedAt()));
    }

    protected final Department toDomain(DepartmentDO dataObject) {
        return Department.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getCode(),
                dataObject.getName(),
                dataObject.getParentId(),
                dataObject.getLeaderUserId(),
                dataObject.getSort(),
                DepartmentStatus.from(dataObject.getStatus()),
                dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final PostDO toDataObject(Post post) {
        return new PostDO(
                post.getId(),
                post.getTenantId(),
                post.getCode(),
                post.getName(),
                post.getDepartmentId(),
                post.getStatus() == null ? null : post.getStatus().value(),
                post.getCreatedBy(),
                toLocalDateTime(post.getCreatedAt()),
                post.getUpdatedBy(),
                toLocalDateTime(post.getUpdatedAt()));
    }

    protected final Post toDomain(PostDO dataObject) {
        return Post.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getCode(),
                dataObject.getName(),
                dataObject.getDepartmentId(),
                PostStatus.from(dataObject.getStatus()),
                dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final RoleDO toDataObject(Role role) {
        return new RoleDO(
                role.getId(),
                role.getTenantId(),
                role.getCode(),
                role.getName(),
                role.getRoleType() == null ? null : role.getRoleType().value(),
                role.getDataScopeType() == null ? null : role.getDataScopeType().value(),
                role.getStatus() == null ? null : role.getStatus().value(),
                role.getCreatedBy(),
                toLocalDateTime(role.getCreatedAt()),
                role.getUpdatedBy(),
                toLocalDateTime(role.getUpdatedAt()));
    }

    protected final Role toDomain(RoleDO dataObject) {
        return Role.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getCode(),
                dataObject.getName(),
                RoleType.from(dataObject.getRoleType()),
                RoleDataScopeType.from(dataObject.getDataScopeType()),
                RoleStatus.from(dataObject.getStatus()),
                dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final MenuDO toDataObject(Menu menu) {
        return new MenuDO(
                menu.getId(),
                menu.getTenantId(),
                menu.getMenuType(),
                menu.getName(),
                menu.getParentId(),
                menu.getRoutePath(),
                menu.getComponentName(),
                menu.getIcon(),
                menu.getSort(),
                menu.getPermissionCode());
    }

    protected final Menu toDomain(MenuDO dataObject) {
        MenuId parentId = dataObject.getParentId();
        return Menu.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getMenuType(),
                dataObject.getName(),
                parentId,
                dataObject.getRoutePath(),
                dataObject.getComponentName(),
                dataObject.getIcon(),
                dataObject.getSort(),
                dataObject.getPermissionCode(),
                List.of());
    }

    protected final ResourceDO toDataObject(Resource resource) {
        return new ResourceDO(
                resource.getId(),
                resource.getTenantId(),
                resource.getCode(),
                resource.getName(),
                resource.getResourceType() == null
                        ? null
                        : resource.getResourceType().value(),
                resource.getHttpMethod(),
                resource.getUri(),
                resource.getStatus() == null ? null : resource.getStatus().value(),
                resource.getCreatedBy(),
                toLocalDateTime(resource.getCreatedAt()),
                resource.getUpdatedBy(),
                toLocalDateTime(resource.getUpdatedAt()));
    }

    protected final Resource toDomain(ResourceDO dataObject) {
        ResourceId resourceId = dataObject.getId();
        return Resource.reconstruct(
                resourceId,
                dataObject.getTenantId(),
                dataObject.getCode(),
                dataObject.getName(),
                ResourceType.from(dataObject.getResourceType()),
                dataObject.getHttpMethod(),
                dataObject.getUri(),
                ResourceStatus.from(dataObject.getStatus()),
                dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final SysLogRecordDO toDataObject(SysLogRecord sysLogRecord) {
        return new SysLogRecordDO(
                sysLogRecord.getId(),
                sysLogRecord.getTenantId(),
                sysLogRecord.getTraceId(),
                sysLogRecord.getRequestId(),
                sysLogRecord.getModule(),
                sysLogRecord.getAction(),
                sysLogRecord.getEventType(),
                sysLogRecord.getResult(),
                sysLogRecord.getOperatorId(),
                sysLogRecord.getOperatorName(),
                sysLogRecord.getClientIp(),
                sysLogRecord.getRequestUri(),
                sysLogRecord.getHttpMethod(),
                sysLogRecord.getCostMs(),
                sysLogRecord.getErrorMessage(),
                sysLogRecord.getOccurredAt(),
                sysLogRecord.getCreatedBy(),
                toLocalDateTime(sysLogRecord.getCreatedAt()),
                sysLogRecord.getUpdatedBy(),
                toLocalDateTime(sysLogRecord.getUpdatedAt()));
    }

    protected final SysLogRecord toDomain(SysLogRecordDO dataObject) {
        return SysLogRecord.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getTraceId(),
                dataObject.getRequestId(),
                dataObject.getModule(),
                dataObject.getAction(),
                dataObject.getEventType(),
                dataObject.getResult(),
                dataObject.getOperatorId(),
                dataObject.getOperatorName(),
                dataObject.getClientIp(),
                dataObject.getRequestUri(),
                dataObject.getHttpMethod(),
                dataObject.getCostMs(),
                dataObject.getErrorMessage(),
                dataObject.getOccurredAt(),
                dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }
}
