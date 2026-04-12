package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DataPermissionRuleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDataScopeRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleMenuRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleResourceRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserRoleRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DataPermissionRuleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.ResourceMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleDataScopeRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMenuRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleResourceRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class RolePersistenceSupport extends AbstractUpmsPersistenceSupport {

    private static final String USER_ROLE_REL_ID_BIZ_TAG = "upms-user-role-rel-id";
    private static final String ROLE_MENU_REL_ID_BIZ_TAG = "upms-role-menu-rel-id";
    private static final String ROLE_RESOURCE_REL_ID_BIZ_TAG = "upms-role-resource-rel-id";
    private static final String ROLE_DATA_SCOPE_REL_ID_BIZ_TAG = "upms-role-data-scope-rel-id";
    private static final String DATA_PERMISSION_RULE_ID_BIZ_TAG = "upms-data-permission-rule-id";

    private final RoleMapper roleMapper;
    private final ResourceMapper resourceMapper;
    private final UserRoleRelMapper userRoleRelMapper;
    private final RoleMenuRelMapper roleMenuRelMapper;
    private final RoleResourceRelMapper roleResourceRelMapper;
    private final DataPermissionRuleMapper dataPermissionRuleMapper;
    private final RoleDataScopeRelMapper roleDataScopeRelMapper;
    private final IdGenerator idGenerator;

    RolePersistenceSupport(
            RoleMapper roleMapper,
            ResourceMapper resourceMapper,
            UserRoleRelMapper userRoleRelMapper,
            RoleMenuRelMapper roleMenuRelMapper,
            RoleResourceRelMapper roleResourceRelMapper,
            DataPermissionRuleMapper dataPermissionRuleMapper,
            RoleDataScopeRelMapper roleDataScopeRelMapper,
            IdGenerator idGenerator) {
        this.roleMapper = roleMapper;
        this.resourceMapper = resourceMapper;
        this.userRoleRelMapper = userRoleRelMapper;
        this.roleMenuRelMapper = roleMenuRelMapper;
        this.roleResourceRelMapper = roleResourceRelMapper;
        this.dataPermissionRuleMapper = dataPermissionRuleMapper;
        this.roleDataScopeRelMapper = roleDataScopeRelMapper;
        this.idGenerator = idGenerator;
    }

    Optional<Role> findRoleById(TenantId tenantId, RoleId roleId) {
        return Optional.ofNullable(roleMapper.selectOne(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .eq(RoleDO::getId, roleId)))
                .map(this::toDomain);
    }

    List<Role> findRolesByUserId(TenantId tenantId, UserId userId) {
        List<RoleId> roleIds = userRoleRelMapper
                .selectList(Wrappers.<UserRoleRelDO>lambdaQuery()
                        .eq(UserRoleRelDO::getTenantId, tenantId)
                        .eq(UserRoleRelDO::getUserId, userId))
                .stream()
                .map(UserRoleRelDO::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper
                .selectList(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .in(RoleDO::getId, roleIds)
                        .orderByAsc(RoleDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    List<UserId> findAssignedUserIds(TenantId tenantId, RoleId roleId) {
        return userRoleRelMapper
                .selectList(Wrappers.<UserRoleRelDO>lambdaQuery()
                        .eq(UserRoleRelDO::getTenantId, tenantId)
                        .eq(UserRoleRelDO::getRoleId, roleId))
                .stream()
                .map(UserRoleRelDO::getUserId)
                .distinct()
                .toList();
    }

    List<Role> listRoles(
            TenantId tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize) {
        return roleMapper
                .selectList(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .like(hasText(code), RoleDO::getCode, code)
                        .like(hasText(name), RoleDO::getName, name)
                        .eq(hasText(roleType), RoleDO::getRoleType, trim(roleType))
                        .eq(hasText(status), RoleDO::getStatus, trim(status))
                        .orderByAsc(RoleDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    long countRoles(TenantId tenantId, String code, String name, String roleType, String status) {
        return Optional.ofNullable(roleMapper.selectCount(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .like(hasText(code), RoleDO::getCode, code)
                        .like(hasText(name), RoleDO::getName, name)
                        .eq(hasText(roleType), RoleDO::getRoleType, trim(roleType))
                        .eq(hasText(status), RoleDO::getStatus, trim(status))))
                .orElse(0L);
    }

    Role saveRole(Role role) {
        RoleDO roleDO = toDataObject(role);
        LocalDateTime now = LocalDateTime.now();
        boolean exists = roleDO.getId() != null && roleMapper.selectById(roleDO.getId()) != null;
        if (!exists) {
            if (roleDO.getCreatedAt() == null) {
                roleDO.setCreatedAt(now);
            }
            roleDO.setUpdatedAt(now);
            roleMapper.insert(roleDO);
        } else {
            roleDO.setUpdatedAt(now);
            roleMapper.updateById(roleDO);
        }
        upsertDataPermissionRule(
                roleDO.getTenantId(), roleDO.getId(), RoleDataScopeType.from(roleDO.getDataScopeType()), now);
        return toDomain(roleDO);
    }

    void deleteRole(TenantId tenantId, RoleId roleId) {
        roleMapper.delete(
                Wrappers.<RoleDO>lambdaQuery().eq(RoleDO::getTenantId, tenantId).eq(RoleDO::getId, roleId));
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery()
                .eq(UserRoleRelDO::getTenantId, tenantId)
                .eq(UserRoleRelDO::getRoleId, roleId));
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery()
                .eq(RoleMenuRelDO::getTenantId, tenantId)
                .eq(RoleMenuRelDO::getRoleId, roleId));
        roleResourceRelMapper.delete(Wrappers.<RoleResourceRelDO>lambdaQuery()
                .eq(RoleResourceRelDO::getTenantId, tenantId)
                .eq(RoleResourceRelDO::getRoleId, roleId));
        roleDataScopeRelMapper.delete(Wrappers.<RoleDataScopeRelDO>lambdaQuery()
                .eq(RoleDataScopeRelDO::getTenantId, tenantId)
                .eq(RoleDataScopeRelDO::getRoleId, roleId));
        dataPermissionRuleMapper.delete(Wrappers.<DataPermissionRuleDO>lambdaQuery()
                .eq(DataPermissionRuleDO::getTenantId, tenantId)
                .eq(DataPermissionRuleDO::getRoleId, roleId));
    }

    void replaceUserRoles(TenantId tenantId, UserId userId, Collection<RoleId> roleIds) {
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery()
                .eq(UserRoleRelDO::getTenantId, tenantId)
                .eq(UserRoleRelDO::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (RoleId roleId : new LinkedHashSet<>(roleIds)) {
            userRoleRelMapper.insert(
                    new UserRoleRelDO(idGenerator.nextId(USER_ROLE_REL_ID_BIZ_TAG), tenantId, userId, roleId));
        }
    }

    void deleteUserRolesByUser(TenantId tenantId, UserId userId) {
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery()
                .eq(UserRoleRelDO::getTenantId, tenantId)
                .eq(UserRoleRelDO::getUserId, userId));
    }

    Set<MenuId> getAssignedMenuIds(TenantId tenantId, RoleId roleId) {
        return roleMenuRelMapper
                .selectList(Wrappers.<RoleMenuRelDO>lambdaQuery()
                        .eq(RoleMenuRelDO::getTenantId, tenantId)
                        .eq(RoleMenuRelDO::getRoleId, roleId))
                .stream()
                .map(RoleMenuRelDO::getMenuId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleMenus(TenantId tenantId, RoleId roleId, Collection<MenuId> menuIds) {
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery()
                .eq(RoleMenuRelDO::getTenantId, tenantId)
                .eq(RoleMenuRelDO::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (MenuId menuId : new LinkedHashSet<>(menuIds)) {
            roleMenuRelMapper.insert(
                    new RoleMenuRelDO(idGenerator.nextId(ROLE_MENU_REL_ID_BIZ_TAG), tenantId, roleId, menuId));
        }
    }

    Set<String> getAssignedResourceCodes(TenantId tenantId, RoleId roleId) {
        List<ResourceId> resourceIds = roleResourceRelMapper
                .selectList(Wrappers.<RoleResourceRelDO>lambdaQuery()
                        .eq(RoleResourceRelDO::getTenantId, tenantId)
                        .eq(RoleResourceRelDO::getRoleId, roleId))
                .stream()
                .map(RoleResourceRelDO::getResourceId)
                .toList();
        if (resourceIds.isEmpty()) {
            return Set.of();
        }
        return resourceMapper
                .selectList(Wrappers.<ResourceDO>lambdaQuery()
                        .eq(ResourceDO::getTenantId, tenantId)
                        .in(ResourceDO::getId, resourceIds))
                .stream()
                .map(ResourceDO::getCode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleResources(TenantId tenantId, RoleId roleId, Collection<String> resourceCodes) {
        roleResourceRelMapper.delete(Wrappers.<RoleResourceRelDO>lambdaQuery()
                .eq(RoleResourceRelDO::getTenantId, tenantId)
                .eq(RoleResourceRelDO::getRoleId, roleId));
        if (resourceCodes == null || resourceCodes.isEmpty()) {
            return;
        }
        List<ResourceDO> resources = resourceMapper.selectList(Wrappers.<ResourceDO>lambdaQuery()
                .eq(ResourceDO::getTenantId, tenantId)
                .in(ResourceDO::getCode, new LinkedHashSet<>(resourceCodes)));
        for (ResourceDO resource : resources) {
            roleResourceRelMapper.insert(new RoleResourceRelDO(
                    idGenerator.nextId(ROLE_RESOURCE_REL_ID_BIZ_TAG), tenantId, roleId, resource.getId()));
        }
    }

    String getAssignedDataScopeType(TenantId tenantId, RoleId roleId) {
        return Optional.ofNullable(dataPermissionRuleMapper.selectOne(Wrappers.<DataPermissionRuleDO>lambdaQuery()
                        .eq(DataPermissionRuleDO::getTenantId, tenantId)
                        .eq(DataPermissionRuleDO::getRoleId, roleId)))
                .map(DataPermissionRuleDO::getDataScopeType)
                .orElse("SELF");
    }

    Set<DepartmentId> getAssignedDataScopeDepartments(TenantId tenantId, RoleId roleId) {
        return roleDataScopeRelMapper
                .selectList(Wrappers.<RoleDataScopeRelDO>lambdaQuery()
                        .eq(RoleDataScopeRelDO::getTenantId, tenantId)
                        .eq(RoleDataScopeRelDO::getRoleId, roleId))
                .stream()
                .map(RoleDataScopeRelDO::getDepartmentId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleDataScope(
            TenantId tenantId, RoleId roleId, RoleDataScopeType dataScopeType, Collection<DepartmentId> departmentIds) {
        LocalDateTime now = LocalDateTime.now();
        upsertDataPermissionRule(tenantId, roleId, dataScopeType, now);
        roleDataScopeRelMapper.delete(Wrappers.<RoleDataScopeRelDO>lambdaQuery()
                .eq(RoleDataScopeRelDO::getTenantId, tenantId)
                .eq(RoleDataScopeRelDO::getRoleId, roleId));
        if (departmentIds == null || departmentIds.isEmpty()) {
            return;
        }
        for (DepartmentId departmentId : new LinkedHashSet<>(departmentIds)) {
            roleDataScopeRelMapper.insert(new RoleDataScopeRelDO(
                    idGenerator.nextId(ROLE_DATA_SCOPE_REL_ID_BIZ_TAG), tenantId, roleId, departmentId));
        }
    }

    void removeMenuFromAssignments(TenantId tenantId, MenuId menuId) {
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery()
                .eq(RoleMenuRelDO::getTenantId, tenantId)
                .eq(RoleMenuRelDO::getMenuId, menuId));
    }

    private void upsertDataPermissionRule(
            TenantId tenantId, RoleId roleId, RoleDataScopeType dataScopeType, LocalDateTime now) {
        DataPermissionRuleDO existing = dataPermissionRuleMapper.selectOne(Wrappers.<DataPermissionRuleDO>lambdaQuery()
                .eq(DataPermissionRuleDO::getTenantId, tenantId)
                .eq(DataPermissionRuleDO::getRoleId, roleId));
        if (existing == null) {
            dataPermissionRuleMapper.insert(new DataPermissionRuleDO(
                    idGenerator.nextId(DATA_PERMISSION_RULE_ID_BIZ_TAG),
                    tenantId,
                    roleId,
                    dataScopeType == null ? null : dataScopeType.value(),
                    null,
                    now,
                    null,
                    now));
            return;
        }
        existing.setDataScopeType(dataScopeType == null ? null : dataScopeType.value());
        existing.setUpdatedAt(now);
        dataPermissionRuleMapper.updateById(existing);
    }
}
