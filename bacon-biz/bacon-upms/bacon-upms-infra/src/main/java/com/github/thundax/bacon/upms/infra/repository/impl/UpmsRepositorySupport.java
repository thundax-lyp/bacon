package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DataPermissionRuleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.PostDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDataScopeRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleMenuRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleResourceRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.SysLogRecordDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.TenantDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserRoleRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DataPermissionRuleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DepartmentMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.MenuMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.PostMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.ResourceMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleDataScopeRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMenuRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleResourceRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.SysLogRecordMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.TenantMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
public class UpmsRepositorySupport {

    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final UserIdentityMapper userIdentityMapper;
    private final DepartmentMapper departmentMapper;
    private final PostMapper postMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final ResourceMapper resourceMapper;
    private final UserRoleRelMapper userRoleRelMapper;
    private final RoleMenuRelMapper roleMenuRelMapper;
    private final RoleResourceRelMapper roleResourceRelMapper;
    private final DataPermissionRuleMapper dataPermissionRuleMapper;
    private final RoleDataScopeRelMapper roleDataScopeRelMapper;
    private final SysLogRecordMapper sysLogRecordMapper;

    public UpmsRepositorySupport(TenantMapper tenantMapper,
                                 UserMapper userMapper,
                                 UserIdentityMapper userIdentityMapper,
                                 DepartmentMapper departmentMapper,
                                 PostMapper postMapper,
                                 RoleMapper roleMapper,
                                 MenuMapper menuMapper,
                                 ResourceMapper resourceMapper,
                                 UserRoleRelMapper userRoleRelMapper,
                                 RoleMenuRelMapper roleMenuRelMapper,
                                 RoleResourceRelMapper roleResourceRelMapper,
                                 DataPermissionRuleMapper dataPermissionRuleMapper,
                                 RoleDataScopeRelMapper roleDataScopeRelMapper,
                                 SysLogRecordMapper sysLogRecordMapper) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.userIdentityMapper = userIdentityMapper;
        this.departmentMapper = departmentMapper;
        this.postMapper = postMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.resourceMapper = resourceMapper;
        this.userRoleRelMapper = userRoleRelMapper;
        this.roleMenuRelMapper = roleMenuRelMapper;
        this.roleResourceRelMapper = roleResourceRelMapper;
        this.dataPermissionRuleMapper = dataPermissionRuleMapper;
        this.roleDataScopeRelMapper = roleDataScopeRelMapper;
        this.sysLogRecordMapper = sysLogRecordMapper;
        log.info("Using MyBatis-Plus upms repository");
    }

    public Optional<Tenant> findTenantByTenantId(Long tenantId) {
        return Optional.ofNullable(tenantMapper.selectOne(Wrappers.<TenantDO>lambdaQuery()
                        .eq(TenantDO::getTenantId, tenantId)))
                .map(this::toDomain);
    }

    public Optional<Tenant> findTenantByCode(String code) {
        return Optional.ofNullable(tenantMapper.selectOne(Wrappers.<TenantDO>lambdaQuery()
                        .eq(TenantDO::getCode, code)))
                .map(this::toDomain);
    }

    public List<Tenant> listTenants(Long tenantId, String code, String name, String status, int pageNo, int pageSize) {
        return tenantMapper.selectList(Wrappers.<TenantDO>lambdaQuery()
                        .eq(tenantId != null, TenantDO::getTenantId, tenantId)
                        .like(hasText(code), TenantDO::getCode, code)
                        .like(hasText(name), TenantDO::getName, name)
                        .eq(hasText(status), TenantDO::getStatus, trim(status))
                        .orderByAsc(TenantDO::getTenantId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public long countTenants(Long tenantId, String code, String name, String status) {
        return Optional.ofNullable(tenantMapper.selectCount(Wrappers.<TenantDO>lambdaQuery()
                        .eq(tenantId != null, TenantDO::getTenantId, tenantId)
                        .like(hasText(code), TenantDO::getCode, code)
                        .like(hasText(name), TenantDO::getName, name)
                        .eq(hasText(status), TenantDO::getStatus, trim(status))))
                .orElse(0L);
    }

    public Tenant saveTenant(Tenant tenant) {
        TenantDO tenantDO = toDataObject(tenant);
        LocalDateTime now = LocalDateTime.now();
        if (tenantDO.getId() == null) {
            tenantDO.setCreatedAt(now);
            tenantDO.setUpdatedAt(now);
            tenantMapper.insert(tenantDO);
        } else {
            tenantDO.setUpdatedAt(now);
            tenantMapper.updateById(tenantDO);
        }
        return toDomain(tenantDO);
    }

    public Optional<User> findUserById(Long tenantId, Long userId) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getId, userId)
                        .eq(UserDO::getDeleted, false)))
                .map(this::toDomain);
    }

    public Optional<User> findUserByAccount(Long tenantId, String account) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getAccount, account)
                        .eq(UserDO::getDeleted, false)))
                .map(this::toDomain);
    }

    public Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue) {
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getTenantId, tenantId)
                        .eq(UserIdentityDO::getIdentityType, identityType)
                        .eq(UserIdentityDO::getIdentityValue, identityValue)
                        .eq(UserIdentityDO::getEnabled, true)))
                .map(this::toDomain);
    }

    public List<User> listUsers(Long tenantId, String account, String name, String phone, String status, int pageNo, int pageSize) {
        return userMapper.selectList(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getDeleted, false)
                        .like(hasText(account), UserDO::getAccount, account)
                        .like(hasText(name), UserDO::getName, name)
                        .like(hasText(phone), UserDO::getPhone, phone)
                        .eq(hasText(status), UserDO::getStatus, trim(status))
                        .orderByAsc(UserDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public long countUsers(Long tenantId, String account, String name, String phone, String status) {
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getDeleted, false)
                        .like(hasText(account), UserDO::getAccount, account)
                        .like(hasText(name), UserDO::getName, name)
                        .like(hasText(phone), UserDO::getPhone, phone)
                        .eq(hasText(status), UserDO::getStatus, trim(status))))
                .orElse(0L);
    }

    public User saveUser(User user) {
        UserDO userDO = toDataObject(user);
        LocalDateTime now = LocalDateTime.now();
        if (userDO.getId() == null) {
            userDO.setCreatedAt(now);
            userDO.setUpdatedAt(now);
            userMapper.insert(userDO);
        } else {
            userDO.setUpdatedAt(now);
            userMapper.updateById(userDO);
        }
        return toDomain(userDO);
    }

    public void deleteUser(Long tenantId, Long userId) {
        userMapper.delete(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getTenantId, tenantId)
                .eq(UserDO::getId, userId));
    }

    public void deleteUserIdentitiesByUser(Long tenantId, Long userId) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getTenantId, tenantId)
                .eq(UserIdentityDO::getUserId, userId));
    }

    public void deleteUserIdentitiesByUserAndType(Long tenantId, Long userId, String identityType) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getTenantId, tenantId)
                .eq(UserIdentityDO::getUserId, userId)
                .eq(UserIdentityDO::getIdentityType, identityType));
    }

    public UserIdentity saveUserIdentity(UserIdentity userIdentity) {
        UserIdentityDO dataObject = toDataObject(userIdentity);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            userIdentityMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            userIdentityMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public boolean hasActiveUserInDepartment(Long tenantId, Long departmentId) {
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getDepartmentId, departmentId)
                        .eq(UserDO::getDeleted, false)))
                .orElse(0L) > 0L;
    }

    public Optional<Department> findDepartmentById(Long tenantId, Long departmentId) {
        return Optional.ofNullable(departmentMapper.selectOne(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getId, departmentId)))
                .map(this::toDomain);
    }

    public Optional<Department> findDepartmentByCode(Long tenantId, String code) {
        return Optional.ofNullable(departmentMapper.selectOne(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getCode, code)))
                .map(this::toDomain);
    }

    public List<Department> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        return departmentMapper.selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .in(DepartmentDO::getId, departmentIds)
                        .orderByAsc(DepartmentDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Department> listDepartmentTree(Long tenantId) {
        return departmentMapper.selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .orderByAsc(DepartmentDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public Department saveDepartment(Department department) {
        DepartmentDO dataObject = toDataObject(department);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            departmentMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            departmentMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public void deleteDepartment(Long tenantId, Long departmentId) {
        departmentMapper.delete(Wrappers.<DepartmentDO>lambdaQuery()
                .eq(DepartmentDO::getTenantId, tenantId)
                .eq(DepartmentDO::getId, departmentId));
    }

    public boolean existsChildDepartment(Long tenantId, Long departmentId) {
        return Optional.ofNullable(departmentMapper.selectCount(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getParentId, departmentId)))
                .orElse(0L) > 0L;
    }

    public Optional<Post> findPostById(Long tenantId, Long postId) {
        return Optional.ofNullable(postMapper.selectOne(Wrappers.<PostDO>lambdaQuery()
                        .eq(PostDO::getTenantId, tenantId)
                        .eq(PostDO::getId, postId)))
                .map(this::toDomain);
    }

    public List<Post> listPosts(Long tenantId, String code, String name, Long departmentId, String status,
                                int pageNo, int pageSize) {
        return postMapper.selectList(Wrappers.<PostDO>lambdaQuery()
                        .eq(tenantId != null, PostDO::getTenantId, tenantId)
                        .like(hasText(code), PostDO::getCode, code)
                        .like(hasText(name), PostDO::getName, name)
                        .eq(departmentId != null, PostDO::getDepartmentId, departmentId)
                        .eq(hasText(status), PostDO::getStatus, trim(status))
                        .orderByAsc(PostDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public long countPosts(Long tenantId, String code, String name, Long departmentId, String status) {
        return Optional.ofNullable(postMapper.selectCount(Wrappers.<PostDO>lambdaQuery()
                        .eq(tenantId != null, PostDO::getTenantId, tenantId)
                        .like(hasText(code), PostDO::getCode, code)
                        .like(hasText(name), PostDO::getName, name)
                        .eq(departmentId != null, PostDO::getDepartmentId, departmentId)
                        .eq(hasText(status), PostDO::getStatus, trim(status))))
                .orElse(0L);
    }

    public Post savePost(Post post) {
        PostDO dataObject = toDataObject(post);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            postMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            postMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public void deletePost(Long tenantId, Long postId) {
        postMapper.delete(Wrappers.<PostDO>lambdaQuery()
                .eq(PostDO::getTenantId, tenantId)
                .eq(PostDO::getId, postId));
    }

    public Optional<Role> findRoleById(Long tenantId, Long roleId) {
        return Optional.ofNullable(roleMapper.selectOne(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .eq(RoleDO::getId, roleId)))
                .map(this::toDomain);
    }

    public List<Role> findRolesByUserId(Long tenantId, Long userId) {
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

    public List<Role> listRoles(Long tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize) {
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

    public long countRoles(Long tenantId, String code, String name, String roleType, String status) {
        return Optional.ofNullable(roleMapper.selectCount(Wrappers.<RoleDO>lambdaQuery()
                        .eq(RoleDO::getTenantId, tenantId)
                        .like(hasText(code), RoleDO::getCode, code)
                        .like(hasText(name), RoleDO::getName, name)
                        .eq(hasText(roleType), RoleDO::getRoleType, trim(roleType))
                        .eq(hasText(status), RoleDO::getStatus, trim(status))))
                .orElse(0L);
    }

    public Role saveRole(Role role) {
        RoleDO roleDO = toDataObject(role);
        LocalDateTime now = LocalDateTime.now();
        if (roleDO.getId() == null) {
            roleDO.setCreatedAt(now);
            roleDO.setUpdatedAt(now);
            roleMapper.insert(roleDO);
            upsertDataPermissionRule(roleDO.getTenantId(), roleDO.getId(), roleDO.getDataScopeType(), now);
        } else {
            roleDO.setUpdatedAt(now);
            roleMapper.updateById(roleDO);
            upsertDataPermissionRule(roleDO.getTenantId(), roleDO.getId(), roleDO.getDataScopeType(), now);
        }
        return toDomain(roleDO);
    }

    public void deleteRole(Long tenantId, Long roleId) {
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

    public void replaceUserRoles(Long tenantId, Long userId, Collection<Long> roleIds) {
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

    public void deleteUserRolesByUser(Long tenantId, Long userId) {
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery()
                .eq(UserRoleRelDO::getTenantId, tenantId)
                .eq(UserRoleRelDO::getUserId, userId));
    }

    public Set<Long> getAssignedMenuIds(Long tenantId, Long roleId) {
        return roleMenuRelMapper.selectList(Wrappers.<RoleMenuRelDO>lambdaQuery()
                        .eq(RoleMenuRelDO::getTenantId, tenantId)
                        .eq(RoleMenuRelDO::getRoleId, roleId))
                .stream()
                .map(RoleMenuRelDO::getMenuId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public void replaceRoleMenus(Long tenantId, Long roleId, Collection<Long> menuIds) {
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

    public Set<String> getAssignedResourceCodes(Long tenantId, Long roleId) {
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

    public void replaceRoleResources(Long tenantId, Long roleId, Collection<String> resourceCodes) {
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

    public String getAssignedDataScopeType(Long tenantId, Long roleId) {
        return Optional.ofNullable(dataPermissionRuleMapper.selectOne(Wrappers.<DataPermissionRuleDO>lambdaQuery()
                        .eq(DataPermissionRuleDO::getTenantId, tenantId)
                        .eq(DataPermissionRuleDO::getRoleId, roleId)))
                .map(DataPermissionRuleDO::getDataScopeType)
                .orElse("SELF");
    }

    public Set<Long> getAssignedDataScopeDepartments(Long tenantId, Long roleId) {
        return roleDataScopeRelMapper.selectList(Wrappers.<RoleDataScopeRelDO>lambdaQuery()
                        .eq(RoleDataScopeRelDO::getTenantId, tenantId)
                        .eq(RoleDataScopeRelDO::getRoleId, roleId))
                .stream()
                .map(RoleDataScopeRelDO::getDepartmentId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public void replaceRoleDataScope(Long tenantId, Long roleId, String dataScopeType, Collection<Long> departmentIds) {
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

    public void removeMenuFromAssignments(Long tenantId, Long menuId) {
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery()
                .eq(RoleMenuRelDO::getTenantId, tenantId)
                .eq(RoleMenuRelDO::getMenuId, menuId));
    }

    public List<Menu> listMenus(Long tenantId) {
        return menuMapper.selectList(Wrappers.<MenuDO>lambdaQuery()
                        .eq(MenuDO::getTenantId, tenantId)
                        .orderByAsc(MenuDO::getSort, MenuDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public Optional<Menu> findMenuById(Long tenantId, Long menuId) {
        return Optional.ofNullable(menuMapper.selectOne(Wrappers.<MenuDO>lambdaQuery()
                        .eq(MenuDO::getTenantId, tenantId)
                        .eq(MenuDO::getId, menuId)))
                .map(this::toDomain);
    }

    public Menu saveMenu(Menu menu) {
        MenuDO dataObject = toDataObject(menu);
        if (dataObject.getId() == null) {
            menuMapper.insert(dataObject);
        } else {
            menuMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public void deleteMenu(Long tenantId, Long menuId) {
        menuMapper.delete(Wrappers.<MenuDO>lambdaQuery()
                .eq(MenuDO::getTenantId, tenantId)
                .eq(MenuDO::getId, menuId));
    }

    public boolean existsChildMenu(Long tenantId, Long menuId) {
        return Optional.ofNullable(menuMapper.selectCount(Wrappers.<MenuDO>lambdaQuery()
                        .eq(MenuDO::getTenantId, tenantId)
                        .eq(MenuDO::getParentId, menuId)))
                .orElse(0L) > 0L;
    }

    public Optional<Resource> findResourceById(Long tenantId, Long resourceId) {
        return Optional.ofNullable(resourceMapper.selectOne(Wrappers.<ResourceDO>lambdaQuery()
                        .eq(ResourceDO::getTenantId, tenantId)
                        .eq(ResourceDO::getId, resourceId)))
                .map(this::toDomain);
    }

    public List<Resource> listResources(Long tenantId, String code, String name, String resourceType, String status,
                                        int pageNo, int pageSize) {
        return resourceMapper.selectList(Wrappers.<ResourceDO>lambdaQuery()
                        .eq(tenantId != null, ResourceDO::getTenantId, tenantId)
                        .like(hasText(code), ResourceDO::getCode, code)
                        .like(hasText(name), ResourceDO::getName, name)
                        .eq(hasText(resourceType), ResourceDO::getResourceType, trim(resourceType))
                        .eq(hasText(status), ResourceDO::getStatus, trim(status))
                        .orderByAsc(ResourceDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public long countResources(Long tenantId, String code, String name, String resourceType, String status) {
        return Optional.ofNullable(resourceMapper.selectCount(Wrappers.<ResourceDO>lambdaQuery()
                        .eq(tenantId != null, ResourceDO::getTenantId, tenantId)
                        .like(hasText(code), ResourceDO::getCode, code)
                        .like(hasText(name), ResourceDO::getName, name)
                        .eq(hasText(resourceType), ResourceDO::getResourceType, trim(resourceType))
                        .eq(hasText(status), ResourceDO::getStatus, trim(status))))
                .orElse(0L);
    }

    public Resource saveResource(Resource resource) {
        ResourceDO dataObject = toDataObject(resource);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            resourceMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            resourceMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public void deleteResource(Long tenantId, Long resourceId) {
        resourceMapper.delete(Wrappers.<ResourceDO>lambdaQuery()
                .eq(ResourceDO::getTenantId, tenantId)
                .eq(ResourceDO::getId, resourceId));
        roleResourceRelMapper.delete(Wrappers.<RoleResourceRelDO>lambdaQuery()
                .eq(RoleResourceRelDO::getTenantId, tenantId)
                .eq(RoleResourceRelDO::getResourceId, resourceId));
    }

    public void saveSysLog(SysLogRecord sysLogRecord) {
        SysLogRecordDO dataObject = toDataObject(sysLogRecord);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            sysLogRecordMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            sysLogRecordMapper.updateById(dataObject);
        }
    }

    public Optional<SysLogRecord> findSysLogById(Long logId) {
        return Optional.ofNullable(sysLogRecordMapper.selectById(logId))
                .map(this::toDomain);
    }

    public List<SysLogRecord> listSysLogs(String tenantId, String module, String eventType, String result,
                                          String operatorName, int pageNo, int pageSize) {
        return sysLogRecordMapper.selectList(Wrappers.<SysLogRecordDO>lambdaQuery()
                        .eq(hasText(tenantId), SysLogRecordDO::getTenantId, trim(tenantId))
                        .eq(hasText(module), SysLogRecordDO::getModule, trim(module))
                        .eq(hasText(eventType), SysLogRecordDO::getEventType, trim(eventType))
                        .eq(hasText(result), SysLogRecordDO::getResult, trim(result))
                        .like(hasText(operatorName), SysLogRecordDO::getOperatorName, operatorName)
                        .orderByDesc(SysLogRecordDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public long countSysLogs(String tenantId, String module, String eventType, String result, String operatorName) {
        return Optional.ofNullable(sysLogRecordMapper.selectCount(Wrappers.<SysLogRecordDO>lambdaQuery()
                        .eq(hasText(tenantId), SysLogRecordDO::getTenantId, trim(tenantId))
                        .eq(hasText(module), SysLogRecordDO::getModule, trim(module))
                        .eq(hasText(eventType), SysLogRecordDO::getEventType, trim(eventType))
                        .eq(hasText(result), SysLogRecordDO::getResult, trim(result))
                        .like(hasText(operatorName), SysLogRecordDO::getOperatorName, operatorName)))
                .orElse(0L);
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

    private String limit(int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNo - 1) * safePageSize;
        return "limit " + offset + "," + safePageSize;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private TenantDO toDataObject(Tenant tenant) {
        return new TenantDO(tenant.getId(), tenant.getTenantId(), tenant.getCode(), tenant.getName(),
                tenant.getStatus(), tenant.getCreatedBy(), tenant.getCreatedAt(), tenant.getUpdatedBy(), tenant.getUpdatedAt());
    }

    private Tenant toDomain(TenantDO tenantDO) {
        return new Tenant(tenantDO.getId(), tenantDO.getTenantId(), tenantDO.getCode(), tenantDO.getName(),
                tenantDO.getStatus(), tenantDO.getCreatedBy(), tenantDO.getCreatedAt(), tenantDO.getUpdatedBy(),
                tenantDO.getUpdatedAt());
    }

    private UserDO toDataObject(User user) {
        return new UserDO(user.getId(), user.getTenantId(), user.getAccount(), user.getName(), user.getPhone(),
                user.getPasswordHash(), user.getDepartmentId(), user.getStatus(), user.isDeleted(), user.getCreatedBy(),
                user.getCreatedAt(), user.getUpdatedBy(), user.getUpdatedAt());
    }

    private User toDomain(UserDO userDO) {
        return new User(userDO.getId(), userDO.getTenantId(), userDO.getAccount(), userDO.getName(), userDO.getPhone(),
                userDO.getPasswordHash(), userDO.getDepartmentId(), userDO.getStatus(), Boolean.TRUE.equals(userDO.getDeleted()),
                userDO.getCreatedBy(), userDO.getCreatedAt(), userDO.getUpdatedBy(), userDO.getUpdatedAt());
    }

    private UserIdentityDO toDataObject(UserIdentity userIdentity) {
        return new UserIdentityDO(userIdentity.getId(), userIdentity.getTenantId(), userIdentity.getUserId(),
                userIdentity.getIdentityType(), userIdentity.getIdentityValue(), userIdentity.isEnabled(),
                userIdentity.getCreatedBy(), userIdentity.getCreatedAt(), userIdentity.getUpdatedBy(), userIdentity.getUpdatedAt());
    }

    private UserIdentity toDomain(UserIdentityDO dataObject) {
        return new UserIdentity(dataObject.getId(), dataObject.getTenantId(), dataObject.getUserId(), dataObject.getIdentityType(),
                dataObject.getIdentityValue(), Boolean.TRUE.equals(dataObject.getEnabled()), dataObject.getCreatedBy(),
                dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    private DepartmentDO toDataObject(Department department) {
        return new DepartmentDO(department.getId(), department.getTenantId(), department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId(), department.getStatus(), department.getCreatedBy(),
                department.getCreatedAt(), department.getUpdatedBy(), department.getUpdatedAt());
    }

    private Department toDomain(DepartmentDO dataObject) {
        return new Department(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getParentId(), dataObject.getLeaderUserId(), dataObject.getStatus(), dataObject.getCreatedBy(),
                dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    private PostDO toDataObject(Post post) {
        return new PostDO(post.getId(), post.getTenantId(), post.getCode(), post.getName(), post.getDepartmentId(),
                post.getStatus(), post.getCreatedBy(), post.getCreatedAt(), post.getUpdatedBy(), post.getUpdatedAt());
    }

    private Post toDomain(PostDO dataObject) {
        return new Post(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getDepartmentId(), dataObject.getStatus(), dataObject.getCreatedBy(), dataObject.getCreatedAt(),
                dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    private RoleDO toDataObject(Role role) {
        return new RoleDO(role.getId(), role.getTenantId(), role.getCode(), role.getName(), role.getRoleType(),
                role.getDataScopeType(), role.getStatus(), role.getCreatedBy(), role.getCreatedAt(), role.getUpdatedBy(),
                role.getUpdatedAt());
    }

    private Role toDomain(RoleDO dataObject) {
        return new Role(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getRoleType(), dataObject.getDataScopeType(), dataObject.getStatus(), dataObject.getCreatedBy(),
                dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    private MenuDO toDataObject(Menu menu) {
        return new MenuDO(menu.getId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode());
    }

    private Menu toDomain(MenuDO dataObject) {
        return new Menu(dataObject.getId(), dataObject.getTenantId(), dataObject.getMenuType(), dataObject.getName(),
                dataObject.getParentId(), dataObject.getRoutePath(), dataObject.getComponentName(), dataObject.getIcon(),
                dataObject.getSort(), dataObject.getPermissionCode(), List.of());
    }

    private ResourceDO toDataObject(Resource resource) {
        return new ResourceDO(resource.getId(), resource.getTenantId(), resource.getCode(), resource.getName(),
                resource.getResourceType(), resource.getHttpMethod(), resource.getUri(), resource.getStatus(),
                resource.getCreatedBy(), resource.getCreatedAt(), resource.getUpdatedBy(), resource.getUpdatedAt());
    }

    private Resource toDomain(ResourceDO dataObject) {
        return new Resource(dataObject.getId(), dataObject.getTenantId(), dataObject.getCode(), dataObject.getName(),
                dataObject.getResourceType(), dataObject.getHttpMethod(), dataObject.getUri(), dataObject.getStatus(),
                dataObject.getCreatedBy(), dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }

    private SysLogRecordDO toDataObject(SysLogRecord sysLogRecord) {
        return new SysLogRecordDO(sysLogRecord.getId(), sysLogRecord.getTenantId(), sysLogRecord.getTraceId(),
                sysLogRecord.getRequestId(), sysLogRecord.getModule(), sysLogRecord.getAction(), sysLogRecord.getEventType(),
                sysLogRecord.getResult(), sysLogRecord.getOperatorId(), sysLogRecord.getOperatorName(), sysLogRecord.getClientIp(),
                sysLogRecord.getRequestUri(), sysLogRecord.getHttpMethod(), sysLogRecord.getCostMs(),
                sysLogRecord.getErrorMessage(), sysLogRecord.getOccurredAt(), sysLogRecord.getCreatedBy(),
                sysLogRecord.getCreatedAt(), sysLogRecord.getUpdatedBy(), sysLogRecord.getUpdatedAt());
    }

    private SysLogRecord toDomain(SysLogRecordDO dataObject) {
        return new SysLogRecord(dataObject.getId(), dataObject.getTenantId(), dataObject.getTraceId(),
                dataObject.getRequestId(), dataObject.getModule(), dataObject.getAction(), dataObject.getEventType(),
                dataObject.getResult(), dataObject.getOperatorId(), dataObject.getOperatorName(), dataObject.getClientIp(),
                dataObject.getRequestUri(), dataObject.getHttpMethod(), dataObject.getCostMs(), dataObject.getErrorMessage(),
                dataObject.getOccurredAt(), dataObject.getCreatedBy(), dataObject.getCreatedAt(), dataObject.getUpdatedBy(),
                dataObject.getUpdatedAt());
    }
}
