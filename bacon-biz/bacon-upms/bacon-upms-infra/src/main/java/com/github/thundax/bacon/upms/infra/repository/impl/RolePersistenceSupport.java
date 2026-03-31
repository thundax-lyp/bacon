package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
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
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
class RolePersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final RoleMapper roleMapper;
    private final ResourceMapper resourceMapper;
    private final UserRoleRelMapper userRoleRelMapper;
    private final RoleMenuRelMapper roleMenuRelMapper;
    private final RoleResourceRelMapper roleResourceRelMapper;
    private final DataPermissionRuleMapper dataPermissionRuleMapper;
    private final RoleDataScopeRelMapper roleDataScopeRelMapper;

    RolePersistenceSupport(RoleMapper roleMapper,
                           ResourceMapper resourceMapper,
                           UserRoleRelMapper userRoleRelMapper,
                           RoleMenuRelMapper roleMenuRelMapper,
                           RoleResourceRelMapper roleResourceRelMapper,
                           DataPermissionRuleMapper dataPermissionRuleMapper,
                           RoleDataScopeRelMapper roleDataScopeRelMapper) {
        this.roleMapper = roleMapper;
        this.resourceMapper = resourceMapper;
        this.userRoleRelMapper = userRoleRelMapper;
        this.roleMenuRelMapper = roleMenuRelMapper;
        this.roleResourceRelMapper = roleResourceRelMapper;
        this.dataPermissionRuleMapper = dataPermissionRuleMapper;
        this.roleDataScopeRelMapper = roleDataScopeRelMapper;
    }

    Optional<Role> findRoleById(Long tenantId, Long roleId) {
        return Optional.ofNullable(roleMapper.selectOne(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .eq(RoleDO::getId, roleId)))
                .map(this::toDomain);
    }

    List<Role> findRolesByUserId(Long tenantId, Long userId) {
        List<Long> roleIds = userRoleRelMapper.selectList(Wrappers.<UserRoleRelDO>lambdaQuery()
                        .eq(UserRoleRelDO::getTenantId, tenantId)
                        .eq(UserRoleRelDO::getUserId, userId))
                .stream()
                .map(UserRoleRelDO::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectList(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .in(RoleDO::getId, roleIds)
                        .orderByAsc(RoleDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    List<Long> findAssignedUserIds(Long tenantId, Long roleId) {
        return userRoleRelMapper.selectList(Wrappers.<UserRoleRelDO>lambdaQuery()
                        .eq(UserRoleRelDO::getTenantId, tenantId)
                        .eq(UserRoleRelDO::getRoleId, roleId))
                .stream()
                .map(UserRoleRelDO::getUserId)
                .distinct()
                .toList();
    }

    List<Role> listRoles(Long tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize) {
        return roleMapper.selectList(Wrappers.<RoleDO>lambdaQuery()
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

    long countRoles(Long tenantId, String code, String name, String roleType, String status) {
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
        if (roleDO.getId() == null) {
            roleDO.setCreatedAt(now);
            roleDO.setUpdatedAt(now);
            roleMapper.insert(roleDO);
        } else {
            roleDO.setUpdatedAt(now);
            roleMapper.updateById(roleDO);
        }
        upsertDataPermissionRule(roleDO.getTenantId(), roleDO.getId(), roleDO.getDataScopeType(), now);
        return toDomain(roleDO);
    }

    void deleteRole(Long tenantId, Long roleId) {
        roleMapper.delete(Wrappers.<RoleDO>lambdaQuery()
                .eq(RoleDO::getTenantId, tenantId)
                .eq(RoleDO::getId, roleId));
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

    void replaceUserRoles(Long tenantId, Long userId, Collection<Long> roleIds) {
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery()
                .eq(UserRoleRelDO::getTenantId, tenantId)
                .eq(UserRoleRelDO::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : new LinkedHashSet<>(roleIds)) {
            userRoleRelMapper.insert(new UserRoleRelDO(null, tenantId, userId, roleId));
        }
    }

    void deleteUserRolesByUser(Long tenantId, Long userId) {
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery()
                .eq(UserRoleRelDO::getTenantId, tenantId)
                .eq(UserRoleRelDO::getUserId, userId));
    }

    Set<Long> getAssignedMenuIds(Long tenantId, Long roleId) {
        return roleMenuRelMapper.selectList(Wrappers.<RoleMenuRelDO>lambdaQuery()
                        .eq(RoleMenuRelDO::getTenantId, tenantId)
                        .eq(RoleMenuRelDO::getRoleId, roleId))
                .stream()
                .map(RoleMenuRelDO::getMenuId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleMenus(Long tenantId, Long roleId, Collection<Long> menuIds) {
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery()
                .eq(RoleMenuRelDO::getTenantId, tenantId)
                .eq(RoleMenuRelDO::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : new LinkedHashSet<>(menuIds)) {
            roleMenuRelMapper.insert(new RoleMenuRelDO(null, tenantId, roleId, menuId));
        }
    }

    Set<String> getAssignedResourceCodes(Long tenantId, Long roleId) {
        List<Long> resourceIds = roleResourceRelMapper.selectList(Wrappers.<RoleResourceRelDO>lambdaQuery()
                        .eq(RoleResourceRelDO::getTenantId, tenantId)
                        .eq(RoleResourceRelDO::getRoleId, roleId))
                .stream()
                .map(RoleResourceRelDO::getResourceId)
                .toList();
        if (resourceIds.isEmpty()) {
            return Set.of();
        }
        return resourceMapper.selectList(Wrappers.<ResourceDO>lambdaQuery()
                        .eq(ResourceDO::getTenantId, tenantId)
                        .in(ResourceDO::getId, resourceIds))
                .stream()
                .map(ResourceDO::getCode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleResources(Long tenantId, Long roleId, Collection<String> resourceCodes) {
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
            roleResourceRelMapper.insert(new RoleResourceRelDO(null, tenantId, roleId, resource.getId()));
        }
    }

    String getAssignedDataScopeType(Long tenantId, Long roleId) {
        return Optional.ofNullable(dataPermissionRuleMapper.selectOne(Wrappers.<DataPermissionRuleDO>lambdaQuery()
                        .eq(DataPermissionRuleDO::getTenantId, tenantId)
                        .eq(DataPermissionRuleDO::getRoleId, roleId)))
                .map(DataPermissionRuleDO::getDataScopeType)
                .orElse("SELF");
    }

    Set<Long> getAssignedDataScopeDepartments(Long tenantId, Long roleId) {
        return roleDataScopeRelMapper.selectList(Wrappers.<RoleDataScopeRelDO>lambdaQuery()
                        .eq(RoleDataScopeRelDO::getTenantId, tenantId)
                        .eq(RoleDataScopeRelDO::getRoleId, roleId))
                .stream()
                .map(RoleDataScopeRelDO::getDepartmentId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    void replaceRoleDataScope(Long tenantId, Long roleId, String dataScopeType, Collection<Long> departmentIds) {
        LocalDateTime now = LocalDateTime.now();
        upsertDataPermissionRule(tenantId, roleId, dataScopeType, now);
        roleDataScopeRelMapper.delete(Wrappers.<RoleDataScopeRelDO>lambdaQuery()
                .eq(RoleDataScopeRelDO::getTenantId, tenantId)
                .eq(RoleDataScopeRelDO::getRoleId, roleId));
        if (departmentIds == null || departmentIds.isEmpty()) {
            return;
        }
        for (Long departmentId : new LinkedHashSet<>(departmentIds)) {
            roleDataScopeRelMapper.insert(new RoleDataScopeRelDO(null, tenantId, roleId, departmentId));
        }
    }

    void removeMenuFromAssignments(Long tenantId, Long menuId) {
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery()
                .eq(RoleMenuRelDO::getTenantId, tenantId)
                .eq(RoleMenuRelDO::getMenuId, menuId));
    }

    private void upsertDataPermissionRule(Long tenantId, Long roleId, String dataScopeType, LocalDateTime now) {
        DataPermissionRuleDO existing = dataPermissionRuleMapper.selectOne(Wrappers.<DataPermissionRuleDO>lambdaQuery()
                .eq(DataPermissionRuleDO::getTenantId, tenantId)
                .eq(DataPermissionRuleDO::getRoleId, roleId));
        if (existing == null) {
            dataPermissionRuleMapper.insert(new DataPermissionRuleDO(null, tenantId, roleId, dataScopeType,
                    null, now, null, now));
            return;
        }
        existing.setDataScopeType(dataScopeType);
        existing.setUpdatedAt(now);
        dataPermissionRuleMapper.updateById(existing);
    }
}
