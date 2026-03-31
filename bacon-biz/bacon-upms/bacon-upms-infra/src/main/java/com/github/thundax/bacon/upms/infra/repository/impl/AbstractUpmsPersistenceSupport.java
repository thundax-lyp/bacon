package com.github.thundax.bacon.upms.infra.repository.impl;

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
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.PostDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.SysLogRecordDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.TenantDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
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
        return new TenantDO(tenant.getId(), tenant.getTenantId(), tenant.getCode(), tenant.getName(),
                tenant.getStatus(), tenant.getCreatedBy(), toLocalDateTime(tenant.getCreatedAt()), tenant.getUpdatedBy(),
                toLocalDateTime(tenant.getUpdatedAt()));
    }

    protected final Tenant toDomain(TenantDO tenantDO) {
        return new Tenant(tenantDO.getId(), tenantDO.getTenantId(), tenantDO.getCode(), tenantDO.getName(),
                tenantDO.getStatus(), tenantDO.getCreatedBy(), toInstant(tenantDO.getCreatedAt()), tenantDO.getUpdatedBy(),
                toInstant(tenantDO.getUpdatedAt()));
    }

    protected final UserDO toDataObject(User user) {
        return new UserDO(user.getId(), user.getTenantId(), user.getAccount(), user.getName(), user.getAvatarObjectId(),
                user.getPhone(), user.getPasswordHash(), user.getDepartmentId(), user.getStatus().value(),
                false, user.getCreatedBy(), toLocalDateTime(user.getCreatedAt()), user.getUpdatedBy(),
                toLocalDateTime(user.getUpdatedAt()));
    }

    protected final User toDomain(UserDO userDO) {
        return new User(userDO.getId(), userDO.getTenantId(), userDO.getAccount(), userDO.getName(),
                userDO.getAvatarObjectId(), userDO.getPhone(), userDO.getPasswordHash(), userDO.getDepartmentId(),
                UserStatus.valueOf(userDO.getStatus()), userDO.getCreatedBy(), toInstant(userDO.getCreatedAt()),
                userDO.getUpdatedBy(), toInstant(userDO.getUpdatedAt()));
    }

    protected final UserIdentityDO toDataObject(UserIdentity userIdentity) {
        return new UserIdentityDO(userIdentity.getId(), userIdentity.getTenantId(), userIdentity.getUserId(),
                userIdentity.getIdentityType(), userIdentity.getIdentityValue(), userIdentity.isEnabled(),
                userIdentity.getCreatedBy(), toLocalDateTime(userIdentity.getCreatedAt()), userIdentity.getUpdatedBy(),
                toLocalDateTime(userIdentity.getUpdatedAt()));
    }

    protected final UserIdentity toDomain(UserIdentityDO dataObject) {
        return new UserIdentity(dataObject.getId(), dataObject.getTenantId(), dataObject.getUserId(), dataObject.getIdentityType(),
                dataObject.getIdentityValue(), Boolean.TRUE.equals(dataObject.getEnabled()), dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()), dataObject.getUpdatedBy(), toInstant(dataObject.getUpdatedAt()));
    }

    protected final UserCredentialDO toDataObject(UserCredential userCredential) {
        return new UserCredentialDO(userCredential.getId(), userCredential.getTenantId(), userCredential.getUserId(),
                userCredential.getIdentityId(), userCredential.getCredentialType(), userCredential.getFactorLevel(),
                userCredential.getCredentialValue(), userCredential.getStatus(), userCredential.isNeedChangePassword(),
                userCredential.getFailedCount(), userCredential.getFailedLimit(), userCredential.getLockReason(),
                userCredential.getLockedUntil(), userCredential.getExpiresAt(), userCredential.getLastVerifiedAt(),
                userCredential.getCreatedBy(), toLocalDateTime(userCredential.getCreatedAt()), userCredential.getUpdatedBy(),
                toLocalDateTime(userCredential.getUpdatedAt()));
    }

    protected final UserCredential toDomain(UserCredentialDO dataObject) {
        return new UserCredential(dataObject.getId(), dataObject.getTenantId(), dataObject.getUserId(),
                dataObject.getIdentityId(), dataObject.getCredentialType(), dataObject.getFactorLevel(),
                dataObject.getCredentialValue(), dataObject.getStatus(), Boolean.TRUE.equals(dataObject.getNeedChangePassword()),
                dataObject.getFailedCount() == null ? 0 : dataObject.getFailedCount(),
                dataObject.getFailedLimit() == null ? 0 : dataObject.getFailedLimit(), dataObject.getLockReason(),
                dataObject.getLockedUntil(), dataObject.getExpiresAt(), dataObject.getLastVerifiedAt(),
                dataObject.getCreatedBy(), toInstant(dataObject.getCreatedAt()), dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final DepartmentDO toDataObject(Department department) {
        return new DepartmentDO(department.getId(), department.getTenantId(), department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId(), department.getStatus(), department.getCreatedBy(),
                toLocalDateTime(department.getCreatedAt()), department.getUpdatedBy(), toLocalDateTime(department.getUpdatedAt()));
    }

    protected final Department toDomain(DepartmentDO dataObject) {
        return new Department(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getParentId(), dataObject.getLeaderUserId(), dataObject.getStatus(), dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()), dataObject.getUpdatedBy(), toInstant(dataObject.getUpdatedAt()));
    }

    protected final PostDO toDataObject(Post post) {
        return new PostDO(post.getId(), post.getTenantId(), post.getCode(), post.getName(), post.getDepartmentId(),
                post.getStatus(), post.getCreatedBy(), toLocalDateTime(post.getCreatedAt()), post.getUpdatedBy(),
                toLocalDateTime(post.getUpdatedAt()));
    }

    protected final Post toDomain(PostDO dataObject) {
        return new Post(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getDepartmentId(), dataObject.getStatus(), dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()), dataObject.getUpdatedBy(), toInstant(dataObject.getUpdatedAt()));
    }

    protected final RoleDO toDataObject(Role role) {
        return new RoleDO(role.getId(), role.getTenantId(), role.getCode(), role.getName(), role.getRoleType(),
                role.getDataScopeType(), role.getStatus(), role.getCreatedBy(), toLocalDateTime(role.getCreatedAt()),
                role.getUpdatedBy(), toLocalDateTime(role.getUpdatedAt()));
    }

    protected final Role toDomain(RoleDO dataObject) {
        return new Role(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getRoleType(), dataObject.getDataScopeType(), dataObject.getStatus(), dataObject.getCreatedBy(),
                toInstant(dataObject.getCreatedAt()), dataObject.getUpdatedBy(), toInstant(dataObject.getUpdatedAt()));
    }

    protected final MenuDO toDataObject(Menu menu) {
        return new MenuDO(menu.getId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode());
    }

    protected final Menu toDomain(MenuDO dataObject) {
        return new Menu(dataObject.getId(), dataObject.getTenantId(), dataObject.getMenuType(), dataObject.getName(),
                dataObject.getParentId(), dataObject.getRoutePath(), dataObject.getComponentName(), dataObject.getIcon(),
                dataObject.getSort(), dataObject.getPermissionCode(), List.of());
    }

    protected final ResourceDO toDataObject(Resource resource) {
        return new ResourceDO(resource.getId(), resource.getTenantId(), resource.getCode(), resource.getName(),
                resource.getResourceType(), resource.getHttpMethod(), resource.getUri(), resource.getStatus(),
                resource.getCreatedBy(), toLocalDateTime(resource.getCreatedAt()), resource.getUpdatedBy(),
                toLocalDateTime(resource.getUpdatedAt()));
    }

    protected final Resource toDomain(ResourceDO dataObject) {
        return new Resource(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getResourceType(), dataObject.getHttpMethod(), dataObject.getUri(), dataObject.getStatus(),
                dataObject.getCreatedBy(), toInstant(dataObject.getCreatedAt()), dataObject.getUpdatedBy(),
                toInstant(dataObject.getUpdatedAt()));
    }

    protected final SysLogRecordDO toDataObject(SysLogRecord sysLogRecord) {
        return new SysLogRecordDO(sysLogRecord.getId(), sysLogRecord.getTenantId(), sysLogRecord.getTraceId(),
                sysLogRecord.getRequestId(), sysLogRecord.getModule(), sysLogRecord.getAction(), sysLogRecord.getEventType(),
                sysLogRecord.getResult(), sysLogRecord.getOperatorId(), sysLogRecord.getOperatorName(), sysLogRecord.getClientIp(),
                sysLogRecord.getRequestUri(), sysLogRecord.getHttpMethod(), sysLogRecord.getCostMs(),
                sysLogRecord.getErrorMessage(), sysLogRecord.getOccurredAt(), sysLogRecord.getCreatedBy(),
                toLocalDateTime(sysLogRecord.getCreatedAt()), sysLogRecord.getUpdatedBy(),
                toLocalDateTime(sysLogRecord.getUpdatedAt()));
    }

    protected final SysLogRecord toDomain(SysLogRecordDO dataObject) {
        return new SysLogRecord(dataObject.getId(), dataObject.getTenantId(), dataObject.getTraceId(),
                dataObject.getRequestId(), dataObject.getModule(), dataObject.getAction(), dataObject.getEventType(),
                dataObject.getResult(), dataObject.getOperatorId(), dataObject.getOperatorName(), dataObject.getClientIp(),
                dataObject.getRequestUri(), dataObject.getHttpMethod(), dataObject.getCostMs(), dataObject.getErrorMessage(),
                dataObject.getOccurredAt(), dataObject.getCreatedBy(), toInstant(dataObject.getCreatedAt()),
                dataObject.getUpdatedBy(), toInstant(dataObject.getUpdatedAt()));
    }
}
