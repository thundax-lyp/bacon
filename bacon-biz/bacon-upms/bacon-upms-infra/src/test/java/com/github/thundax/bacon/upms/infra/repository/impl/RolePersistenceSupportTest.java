package com.github.thundax.bacon.upms.infra.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RolePersistenceSupportTest {

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

    @Mock
    private IdGenerator idGenerator;

    private RolePersistenceSupport support;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContextHolder.BaconContext(1001L, 2001L));
        support = new RolePersistenceSupport(
                roleMapper,
                resourceMapper,
                userRoleRelMapper,
                roleMenuRelMapper,
                roleResourceRelMapper,
                dataPermissionRuleMapper,
                roleDataScopeRelMapper,
                idGenerator);
        Mockito.lenient().when(idGenerator.nextId(any())).thenReturn(1001L, 1002L, 1003L, 1004L, 1005L);
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldReplaceRoleDataScopeRuleAndDepartments() {
        ArgumentCaptor<DataPermissionRuleDO> ruleCaptor = ArgumentCaptor.forClass(DataPermissionRuleDO.class);
        ArgumentCaptor<RoleDataScopeRelDO> relationCaptor = ArgumentCaptor.forClass(RoleDataScopeRelDO.class);
        when(dataPermissionRuleMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        support.replaceRoleDataScope(
                RoleId.of(9L), RoleDataScopeType.CUSTOM, Set.of(DepartmentId.of(11L), DepartmentId.of(12L)));

        verify(dataPermissionRuleMapper).insert(ruleCaptor.capture());
        verify(roleDataScopeRelMapper, Mockito.times(2)).insert(relationCaptor.capture());
        assertThat(ruleCaptor.getValue().getTenantId()).isEqualTo(1001L);
        assertThat(ruleCaptor.getValue().getRoleId()).isEqualTo(9L);
        assertThat(ruleCaptor.getValue().getDataScopeType()).isEqualTo("CUSTOM");
        assertThat(relationCaptor.getAllValues())
                .extracting(RoleDataScopeRelDO::getRoleId)
                .containsOnly(9L);
        assertThat(relationCaptor.getAllValues())
                .extracting(RoleDataScopeRelDO::getDepartmentId)
                .containsExactlyInAnyOrder(11L, 12L);
    }

    @Test
    void shouldResolveAssignedResourceCodesFromRelationRows() {
        when(roleResourceRelMapper.selectList(any(Wrapper.class)))
                .thenReturn(
                        List.of(new RoleResourceRelDO(1L, 1001L, 9L, 21L), new RoleResourceRelDO(2L, 1001L, 9L, 22L)));
        when(resourceMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(
                        new ResourceDO(21L, 1001L, "upms:user:view", "User View", "API", "GET", "/users", "ACTIVE"),
                        new ResourceDO(22L, 1001L, "upms:user:edit", "User Edit", "API", "POST", "/users", "ACTIVE")));

        Set<ResourceCode> assignedResourceCodes = support.findResourceCodes(RoleId.of(9L));

        assertThat(assignedResourceCodes).containsExactlyInAnyOrder(
                ResourceCode.of("upms:user:view"), ResourceCode.of("upms:user:edit"));
    }

    @Test
    void shouldPersistRoleResourceRelationsByResourceCode() {
        ArgumentCaptor<RoleResourceRelDO> captor = ArgumentCaptor.forClass(RoleResourceRelDO.class);
        when(resourceMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(
                        new ResourceDO(21L, 1001L, "upms:user:view", "User View", "API", "GET", "/users", "ACTIVE")));

        support.replaceRoleResources(RoleId.of(9L), Set.of(ResourceCode.of("upms:user:view")));

        verify(roleResourceRelMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(1001L);
        assertThat(captor.getValue().getRoleId()).isEqualTo(9L);
        assertThat(captor.getValue().getResourceId()).isEqualTo(21L);
    }
}
