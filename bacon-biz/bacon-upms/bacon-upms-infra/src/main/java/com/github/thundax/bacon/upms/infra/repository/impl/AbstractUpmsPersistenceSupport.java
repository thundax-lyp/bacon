package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
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
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
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

    protected final TenantDO toDataObject(Tenant tenant) {
        return new TenantDO(tenant.getId(), tenant.getTenantId(), tenant.getCode(), tenant.getName(),
                tenant.getStatus(), tenant.getCreatedBy(), tenant.getCreatedAt(), tenant.getUpdatedBy(), tenant.getUpdatedAt());
    }

    protected final Tenant toDomain(TenantDO tenantDO) {
        return new Tenant(tenantDO.getId(), tenantDO.getTenantId(), tenantDO.getCode(), tenantDO.getName(),
                tenantDO.getStatus(), tenantDO.getCreatedBy(), tenantDO.getCreatedAt(), tenantDO.getUpdatedBy(),
                tenantDO.getUpdatedAt());
    }

    protected final UserDO toDataObject(User user) {
        return new UserDO(user.getId(), user.getTenantId(), user.getAccount(), user.getName(), user.getAvatarObjectId(),
                user.getPhone(), user.getPasswordHash(), user.getDepartmentId(), user.getStatus().value(),
                false, user.getCreatedBy(), user.getCreatedAt(), user.getUpdatedBy(), user.getUpdatedAt());
    }

    protected final User toDomain(UserDO userDO) {
        return new User(userDO.getId(), userDO.getTenantId(), userDO.getAccount(), userDO.getName(),
                userDO.getAvatarObjectId(), userDO.getPhone(), userDO.getPasswordHash(), userDO.getDepartmentId(),
                UserStatus.valueOf(userDO.getStatus()), userDO.getCreatedBy(), userDO.getCreatedAt(),
                userDO.getUpdatedBy(), userDO.getUpdatedAt());
    }

    protected final UserIdentityDO toDataObject(UserIdentity userIdentity) {
        return new UserIdentityDO(userIdentity.getId(), userIdentity.getTenantId(), userIdentity.getUserId(),
                userIdentity.getIdentityType(), userIdentity.getIdentityValue(), userIdentity.isEnabled(),
                userIdentity.getPasswordHash(), userIdentity.getCreatedBy(), userIdentity.getCreatedAt(),
                userIdentity.getUpdatedBy(), userIdentity.getUpdatedAt());
    }

    protected final UserIdentity toDomain(UserIdentityDO dataObject) {
        return new UserIdentity(dataObject.getId(), dataObject.getTenantId(), dataObject.getUserId(), dataObject.getIdentityType(),
                dataObject.getIdentityValue(), Boolean.TRUE.equals(dataObject.getEnabled()), dataObject.getPasswordHash(),
                dataObject.getCreatedBy(), dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    protected final DepartmentDO toDataObject(Department department) {
        return new DepartmentDO(department.getId(), department.getTenantId(), department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId(), department.getStatus(), department.getCreatedBy(),
                department.getCreatedAt(), department.getUpdatedBy(), department.getUpdatedAt());
    }

    protected final Department toDomain(DepartmentDO dataObject) {
        return new Department(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getParentId(), dataObject.getLeaderUserId(), dataObject.getStatus(), dataObject.getCreatedBy(),
                dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    protected final PostDO toDataObject(Post post) {
        return new PostDO(post.getId(), post.getTenantId(), post.getCode(), post.getName(), post.getDepartmentId(),
                post.getStatus(), post.getCreatedBy(), post.getCreatedAt(), post.getUpdatedBy(), post.getUpdatedAt());
    }

    protected final Post toDomain(PostDO dataObject) {
        return new Post(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getDepartmentId(), dataObject.getStatus(), dataObject.getCreatedBy(), dataObject.getCreatedAt(),
                dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    protected final RoleDO toDataObject(Role role) {
        return new RoleDO(role.getId(), role.getTenantId(), role.getCode(), role.getName(), role.getRoleType(),
                role.getDataScopeType(), role.getStatus(), role.getCreatedBy(), role.getCreatedAt(), role.getUpdatedBy(),
                role.getUpdatedAt());
    }

    protected final Role toDomain(RoleDO dataObject) {
        return new Role(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getRoleType(), dataObject.getDataScopeType(), dataObject.getStatus(), dataObject.getCreatedBy(),
                dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
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
                resource.getCreatedBy(), resource.getCreatedAt(), resource.getUpdatedBy(), resource.getUpdatedAt());
    }

    protected final Resource toDomain(ResourceDO dataObject) {
        return new Resource(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getResourceType(), dataObject.getHttpMethod(), dataObject.getUri(), dataObject.getStatus(),
                dataObject.getCreatedBy(), dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    protected final SysLogRecordDO toDataObject(SysLogRecord sysLogRecord) {
        return new SysLogRecordDO(sysLogRecord.getId(), sysLogRecord.getTenantId(), sysLogRecord.getTraceId(),
                sysLogRecord.getRequestId(), sysLogRecord.getModule(), sysLogRecord.getAction(), sysLogRecord.getEventType(),
                sysLogRecord.getResult(), sysLogRecord.getOperatorId(), sysLogRecord.getOperatorName(), sysLogRecord.getClientIp(),
                sysLogRecord.getRequestUri(), sysLogRecord.getHttpMethod(), sysLogRecord.getCostMs(),
                sysLogRecord.getErrorMessage(), sysLogRecord.getOccurredAt(), sysLogRecord.getCreatedBy(),
                sysLogRecord.getCreatedAt(), sysLogRecord.getUpdatedBy(), sysLogRecord.getUpdatedAt());
    }

    protected final SysLogRecord toDomain(SysLogRecordDO dataObject) {
        return new SysLogRecord(dataObject.getId(), dataObject.getTenantId(), dataObject.getTraceId(),
                dataObject.getRequestId(), dataObject.getModule(), dataObject.getAction(), dataObject.getEventType(),
                dataObject.getResult(), dataObject.getOperatorId(), dataObject.getOperatorName(), dataObject.getClientIp(),
                dataObject.getRequestUri(), dataObject.getHttpMethod(), dataObject.getCostMs(), dataObject.getErrorMessage(),
                dataObject.getOccurredAt(), dataObject.getCreatedBy(), dataObject.getCreatedAt(), dataObject.getUpdatedBy(),
                dataObject.getUpdatedAt());
    }
}
