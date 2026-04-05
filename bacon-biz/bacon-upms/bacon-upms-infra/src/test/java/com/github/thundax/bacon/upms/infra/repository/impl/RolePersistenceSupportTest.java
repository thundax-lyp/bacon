package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DataPermissionRuleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDataScopeRelDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleResourceRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DataPermissionRuleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.ResourceMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleDataScopeRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMenuRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleResourceRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePersistenceSupportTest {

    private static final TenantId TENANT_ID = TenantId.of(1001L);

    @Mock
    private RoleMapper roleMapper;
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

    private RolePersistenceSupport support;

    @BeforeEach
    void setUp() {
        support = new RolePersistenceSupport(roleMapper, resourceMapper, userRoleRelMapper, roleMenuRelMapper,
                roleResourceRelMapper, dataPermissionRuleMapper, roleDataScopeRelMapper);
    }

    @Test
    void shouldReplaceRoleDataScopeRuleAndDepartments() {
        ArgumentCaptor<DataPermissionRuleDO> ruleCaptor = ArgumentCaptor.forClass(DataPermissionRuleDO.class);
        ArgumentCaptor<RoleDataScopeRelDO> relationCaptor = ArgumentCaptor.forClass(RoleDataScopeRelDO.class);
        when(dataPermissionRuleMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        support.replaceRoleDataScope(TENANT_ID, RoleId.of(9L), RoleDataScopeType.CUSTOM,
                Set.of(DepartmentId.of(11L), DepartmentId.of(12L)));

        verify(dataPermissionRuleMapper).insert(ruleCaptor.capture());
        verify(roleDataScopeRelMapper, Mockito.times(2)).insert(relationCaptor.capture());
        assertThat(ruleCaptor.getValue().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(ruleCaptor.getValue().getRoleId()).isEqualTo(RoleId.of(9L));
        assertThat(ruleCaptor.getValue().getDataScopeType()).isEqualTo("CUSTOM");
        assertThat(relationCaptor.getAllValues()).extracting(RoleDataScopeRelDO::getRoleId).containsOnly(RoleId.of(9L));
        assertThat(relationCaptor.getAllValues()).extracting(RoleDataScopeRelDO::getDepartmentId)
                .containsExactlyInAnyOrder(DepartmentId.of(11L), DepartmentId.of(12L));
    }

    @Test
    void shouldResolveAssignedResourceCodesFromRelationRows() {
        when(roleResourceRelMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                new RoleResourceRelDO(1L, TENANT_ID, RoleId.of(9L), ResourceId.of(21L)),
                new RoleResourceRelDO(2L, TENANT_ID, RoleId.of(9L), ResourceId.of(22L))));
        when(resourceMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                new ResourceDO(ResourceId.of(21L), TENANT_ID, "upms:user:view", "User View", "API", "GET", "/users", "ACTIVE",
                        null, null, null, null),
                new ResourceDO(ResourceId.of(22L), TENANT_ID, "upms:user:edit", "User Edit", "API", "POST", "/users", "ACTIVE",
                        null, null, null, null)));

        Set<String> assignedResourceCodes = support.getAssignedResourceCodes(TENANT_ID, RoleId.of(9L));

        assertThat(assignedResourceCodes).containsExactlyInAnyOrder("upms:user:view", "upms:user:edit");
    }

    @Test
    void shouldPersistRoleResourceRelationsByResourceCode() {
        ArgumentCaptor<RoleResourceRelDO> captor = ArgumentCaptor.forClass(RoleResourceRelDO.class);
        when(resourceMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                new ResourceDO(ResourceId.of(21L), TENANT_ID, "upms:user:view", "User View", "API", "GET", "/users", "ACTIVE",
                        null, null, null, null)));

        support.replaceRoleResources(TENANT_ID, RoleId.of(9L), Set.of("upms:user:view"));

        verify(roleResourceRelMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(captor.getValue().getRoleId()).isEqualTo(RoleId.of(9L));
        assertThat(captor.getValue().getResourceId()).isEqualTo(ResourceId.of(21L));
    }
}
