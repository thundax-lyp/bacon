package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.RolePersistenceAssembler;
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

    Optional<Role> findRoleById(RoleId roleId) {
        requireTenantId();
        return Optional.ofNullable(
                        roleMapper.selectOne(Wrappers.<RoleDO>lambdaQuery().eq(RoleDO::getId, roleId.value())))
                .map(RolePersistenceAssembler::toDomain);
    }

    List<Role> findRolesByUserId(UserId userId) {
        requireTenantId();
        List<Long> roleIds =
                userRoleRelMapper
                        .selectList(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getUserId, userId.value()))
                        .stream()
                        .map(UserRoleRelDO::getRoleId)
                        .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper
                .selectList(Wrappers.<RoleDO>lambdaQuery()
                        .in(RoleDO::getId, roleIds)
                        .orderByAsc(RoleDO::getId))
                .stream()
                .map(RolePersistenceAssembler::toDomain)
                .toList();
    }

    List<UserId> findAssignedUserIds(TenantId tenantId, RoleId roleId) {
        return userRoleRelMapper
                .selectList(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getRoleId, roleId.value()))
                .stream()
                .map(UserRoleRelDO::getUserId)
                .map(UserId::of)
                .distinct()
                .toList();
    }

    List<Role> listRoles(String code, String name, RoleType roleType, RoleStatus status, int pageNo, int pageSize) {
        return roleMapper
                .selectList(Wrappers.<RoleDO>lambdaQuery()
                        .like(hasText(code), RoleDO::getCode, code)
                        .like(hasText(name), RoleDO::getName, name)
                        .eq(roleType != null, RoleDO::getRoleType, roleType.value())
                        .eq(status != null, RoleDO::getStatus, status.value())
                        .orderByAsc(RoleDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(RolePersistenceAssembler::toDomain)
                .toList();
    }

    long countRoles(String code, String name, RoleType roleType, RoleStatus status) {
        return Optional.ofNullable(roleMapper.selectCount(Wrappers.<RoleDO>lambdaQuery()
                        .like(hasText(code), RoleDO::getCode, code)
                        .like(hasText(name), RoleDO::getName, name)
                        .eq(roleType != null, RoleDO::getRoleType, roleType.value())
                        .eq(status != null, RoleDO::getStatus, status.value())))
                .orElse(0L);
    }

    Role insertRole(Role role) {
        RoleDO roleDO = RolePersistenceAssembler.toDataObject(role);
        roleMapper.insert(roleDO);
        upsertDataPermissionRule(
                TenantId.of(roleDO.getTenantId()),
                RoleId.of(roleDO.getId()),
                RoleDataScopeType.from(roleDO.getDataScopeType()));
        return RolePersistenceAssembler.toDomain(roleDO);
    }

    Role updateRole(Role role) {
        RoleDO roleDO = RolePersistenceAssembler.toDataObject(role);
        roleMapper.updateById(roleDO);
        upsertDataPermissionRule(
                TenantId.of(roleDO.getTenantId()),
                RoleId.of(roleDO.getId()),
                RoleDataScopeType.from(roleDO.getDataScopeType()));
        return RolePersistenceAssembler.toDomain(roleDO);
    }

    void deleteRole(RoleId roleId) {
        requireTenantId();
        roleMapper.delete(Wrappers.<RoleDO>lambdaQuery().eq(RoleDO::getId, roleId.value()));
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getRoleId, roleId.value()));
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getRoleId, roleId.value()));
        roleResourceRelMapper.delete(
                Wrappers.<RoleResourceRelDO>lambdaQuery().eq(RoleResourceRelDO::getRoleId, roleId.value()));
        roleDataScopeRelMapper.delete(
                Wrappers.<RoleDataScopeRelDO>lambdaQuery().eq(RoleDataScopeRelDO::getRoleId, roleId.value()));
        dataPermissionRuleMapper.delete(
                Wrappers.<DataPermissionRuleDO>lambdaQuery().eq(DataPermissionRuleDO::getRoleId, roleId.value()));
    }

    void replaceUserRoles(UserId userId, Collection<RoleId> roleIds) {
        TenantId tenantId = requireTenantId();
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getUserId, userId.value()));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (RoleId roleId : new LinkedHashSet<>(roleIds)) {
            userRoleRelMapper.insert(new UserRoleRelDO(
                    idGenerator.nextId(USER_ROLE_REL_ID_BIZ_TAG), tenantId.value(), userId.value(), roleId.value()));
        }
    }

    void deleteUserRolesByUser(UserId userId) {
        requireTenantId();
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getUserId, userId.value()));
    }

    Set<MenuId> getAssignedMenuIds(RoleId roleId) {
        requireTenantId();
        return roleMenuRelMapper
                .selectList(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getRoleId, roleId.value()))
                .stream()
                .map(RoleMenuRelDO::getMenuId)
                .map(MenuId::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleMenus(RoleId roleId, Collection<MenuId> menuIds) {
        TenantId tenantId = requireTenantId();
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getRoleId, roleId.value()));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (MenuId menuId : new LinkedHashSet<>(menuIds)) {
            roleMenuRelMapper.insert(new RoleMenuRelDO(
                    idGenerator.nextId(ROLE_MENU_REL_ID_BIZ_TAG), tenantId.value(), roleId.value(), menuId.value()));
        }
    }

    Set<String> getAssignedResourceCodes(RoleId roleId) {
        requireTenantId();
        List<Long> resourceIds = roleResourceRelMapper
                .selectList(Wrappers.<RoleResourceRelDO>lambdaQuery().eq(RoleResourceRelDO::getRoleId, roleId.value()))
                .stream()
                .map(RoleResourceRelDO::getResourceId)
                .toList();
        if (resourceIds.isEmpty()) {
            return Set.of();
        }
        return resourceMapper.selectList(Wrappers.<ResourceDO>lambdaQuery().in(ResourceDO::getId, resourceIds)).stream()
                .map(ResourceDO::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleResources(RoleId roleId, Collection<String> resourceCodes) {
        TenantId tenantId = requireTenantId();
        roleResourceRelMapper.delete(
                Wrappers.<RoleResourceRelDO>lambdaQuery().eq(RoleResourceRelDO::getRoleId, roleId.value()));
        if (resourceCodes == null || resourceCodes.isEmpty()) {
            return;
        }
        List<ResourceDO> resources = resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery().in(ResourceDO::getCode, new LinkedHashSet<>(resourceCodes)));
        for (ResourceDO resource : resources) {
            roleResourceRelMapper.insert(new RoleResourceRelDO(
                    idGenerator.nextId(ROLE_RESOURCE_REL_ID_BIZ_TAG),
                    tenantId.value(),
                    roleId.value(),
                    resource.getId()));
        }
    }

    RoleDataScopeType getAssignedDataScopeType(RoleId roleId) {
        requireTenantId();
        return Optional.ofNullable(dataPermissionRuleMapper.selectOne(Wrappers.<DataPermissionRuleDO>lambdaQuery()
                        .eq(DataPermissionRuleDO::getRoleId, roleId.value())))
                .map(DataPermissionRuleDO::getDataScopeType)
                .map(RoleDataScopeType::from)
                .orElse(RoleDataScopeType.SELF);
    }

    Set<DepartmentId> getAssignedDataScopeDepartments(RoleId roleId) {
        requireTenantId();
        return roleDataScopeRelMapper
                .selectList(
                        Wrappers.<RoleDataScopeRelDO>lambdaQuery().eq(RoleDataScopeRelDO::getRoleId, roleId.value()))
                .stream()
                .map(RoleDataScopeRelDO::getDepartmentId)
                .map(DepartmentId::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleDataScope(RoleId roleId, RoleDataScopeType dataScopeType, Collection<DepartmentId> departmentIds) {
        TenantId tenantId = requireTenantId();
        upsertDataPermissionRule(tenantId, roleId, dataScopeType);
        roleDataScopeRelMapper.delete(
                Wrappers.<RoleDataScopeRelDO>lambdaQuery().eq(RoleDataScopeRelDO::getRoleId, roleId.value()));
        if (departmentIds == null || departmentIds.isEmpty()) {
            return;
        }
        for (DepartmentId departmentId : new LinkedHashSet<>(departmentIds)) {
            roleDataScopeRelMapper.insert(new RoleDataScopeRelDO(
                    idGenerator.nextId(ROLE_DATA_SCOPE_REL_ID_BIZ_TAG),
                    tenantId.value(),
                    roleId.value(),
                    departmentId.value()));
        }
    }

    void removeMenuFromAssignments(MenuId menuId) {
        requireTenantId();
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getMenuId, menuId.value()));
    }

    private void upsertDataPermissionRule(TenantId tenantId, RoleId roleId, RoleDataScopeType dataScopeType) {
        DataPermissionRuleDO existing = dataPermissionRuleMapper.selectOne(
                Wrappers.<DataPermissionRuleDO>lambdaQuery().eq(DataPermissionRuleDO::getRoleId, roleId.value()));
        if (existing == null) {
            dataPermissionRuleMapper.insert(new DataPermissionRuleDO(
                    idGenerator.nextId(DATA_PERMISSION_RULE_ID_BIZ_TAG),
                    tenantId.value(),
                    roleId.value(),
                    dataScopeType == null ? null : dataScopeType.value()));
            return;
        }
        existing.setDataScopeType(dataScopeType == null ? null : dataScopeType.value());
        dataPermissionRuleMapper.updateById(existing);
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
