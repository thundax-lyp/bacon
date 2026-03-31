package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DataPermissionRuleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDataScopeRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleResourceRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
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
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpmsRepositorySupportTest {

    @Mock
    private TenantMapper tenantMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserIdentityMapper userIdentityMapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private PostMapper postMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private MenuMapper menuMapper;
    @Mock
    private ResourceMapper resourceMapper;
    @Mock
    private UserRoleRelMapper userRoleRelMapper;
    @Mock
    private RoleMenuRelMapper roleMenuRelMapper;
    @Mock
    private RoleResourceRelMapper roleResourceRelMapper;
    @Mock
    private DataPermissionRuleMapper dataPermissionRuleMapper;
    @Mock
    private RoleDataScopeRelMapper roleDataScopeRelMapper;
    @Mock
    private SysLogRecordMapper sysLogRecordMapper;

    private UpmsRepositorySupport support;

    @BeforeEach
    void setUp() {
        support = new UpmsRepositorySupport(tenantMapper, userMapper, userIdentityMapper, departmentMapper, postMapper,
                roleMapper, menuMapper, resourceMapper, userRoleRelMapper, roleMenuRelMapper, roleResourceRelMapper,
                dataPermissionRuleMapper, roleDataScopeRelMapper, sysLogRecordMapper);
    }

    @Test
    void shouldInsertUserAndMapGeneratedId() {
        ArgumentCaptor<UserDO> captor = ArgumentCaptor.forClass(UserDO.class);
        User newUser = new User(null, 1L, "alice", "Alice", "13800000001", "ENC", 11L, "ACTIVE", false);

        when(userMapper.insert(any(UserDO.class))).thenAnswer(invocation -> {
            UserDO dataObject = invocation.getArgument(0);
            dataObject.setId(101L);
            return 1;
        });

        User savedUser = support.saveUser(newUser);

        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(101L);
        assertThat(savedUser.getAccount()).isEqualTo("alice");
    }

    @Test
    void shouldReplaceRoleDataScopeRuleAndDepartments() {
        ArgumentCaptor<DataPermissionRuleDO> ruleCaptor = ArgumentCaptor.forClass(DataPermissionRuleDO.class);
        ArgumentCaptor<RoleDataScopeRelDO> relationCaptor = ArgumentCaptor.forClass(RoleDataScopeRelDO.class);
        when(dataPermissionRuleMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        support.replaceRoleDataScope(1L, 9L, "CUSTOM", Set.of(11L, 12L));

        verify(dataPermissionRuleMapper).insert(ruleCaptor.capture());
        verify(roleDataScopeRelMapper, Mockito.times(2)).insert(relationCaptor.capture());
        assertThat(ruleCaptor.getValue().getTenantId()).isEqualTo(1L);
        assertThat(ruleCaptor.getValue().getRoleId()).isEqualTo(9L);
        assertThat(ruleCaptor.getValue().getDataScopeType()).isEqualTo("CUSTOM");
        assertThat(relationCaptor.getAllValues()).extracting(RoleDataScopeRelDO::getRoleId).containsOnly(9L);
        assertThat(relationCaptor.getAllValues()).extracting(RoleDataScopeRelDO::getDepartmentId)
                .containsExactlyInAnyOrder(11L, 12L);
    }

    @Test
    void shouldResolveAssignedResourceCodesFromRelationRows() {
        when(roleResourceRelMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                new RoleResourceRelDO(1L, 1L, 9L, 21L),
                new RoleResourceRelDO(2L, 1L, 9L, 22L)));
        when(resourceMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                new ResourceDO(21L, 1L, "upms:user:view", "User View", "API", "GET", "/users", "ACTIVE", null, null, null, null),
                new ResourceDO(22L, 1L, "upms:user:edit", "User Edit", "API", "POST", "/users", "ACTIVE", null, null, null, null)));

        Set<String> assignedResourceCodes = support.getAssignedResourceCodes(1L, 9L);

        assertThat(assignedResourceCodes).containsExactlyInAnyOrder("upms:user:view", "upms:user:edit");
    }

    @Test
    void shouldPersistRoleResourceRelationsByResourceCode() {
        ArgumentCaptor<RoleResourceRelDO> captor = ArgumentCaptor.forClass(RoleResourceRelDO.class);
        when(resourceMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                new ResourceDO(21L, 1L, "upms:user:view", "User View", "API", "GET", "/users", "ACTIVE", null, null, null, null)));

        support.replaceRoleResources(1L, 9L, Set.of("upms:user:view"));

        verify(roleResourceRelMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(1L);
        assertThat(captor.getValue().getRoleId()).isEqualTo(9L);
        assertThat(captor.getValue().getResourceId()).isEqualTo(21L);
    }
}
