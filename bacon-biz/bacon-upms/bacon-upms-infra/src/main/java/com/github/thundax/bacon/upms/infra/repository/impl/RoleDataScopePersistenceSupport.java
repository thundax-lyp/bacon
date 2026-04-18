package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DataPermissionRuleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDataScopeRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DataPermissionRuleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleDataScopeRelMapper;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class RoleDataScopePersistenceSupport extends AbstractUpmsPersistenceSupport {

    private static final String ROLE_DATA_SCOPE_REL_ID_BIZ_TAG = "upms-role-data-scope-rel-id";
    private static final String DATA_PERMISSION_RULE_ID_BIZ_TAG = "upms-data-permission-rule-id";

    private final DataPermissionRuleMapper dataPermissionRuleMapper;
    private final RoleDataScopeRelMapper roleDataScopeRelMapper;
    private final IdGenerator idGenerator;

    RoleDataScopePersistenceSupport(
            DataPermissionRuleMapper dataPermissionRuleMapper,
            RoleDataScopeRelMapper roleDataScopeRelMapper,
            IdGenerator idGenerator) {
        this.dataPermissionRuleMapper = dataPermissionRuleMapper;
        this.roleDataScopeRelMapper = roleDataScopeRelMapper;
        this.idGenerator = idGenerator;
    }

    RoleDataScopeType findDataScopeType(RoleId roleId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(dataPermissionRuleMapper.selectOne(
                        Wrappers.<DataPermissionRuleDO>lambdaQuery().eq(DataPermissionRuleDO::getRoleId, roleId.value())))
                .map(DataPermissionRuleDO::getDataScopeType)
                .map(RoleDataScopeType::from)
                .orElse(RoleDataScopeType.SELF);
    }

    Set<DepartmentId> findDataScopeDepartmentIds(RoleId roleId) {
        BaconContextHolder.requireTenantId();
        Set<DepartmentId> departmentIds = roleDataScopeRelMapper
                .selectList(
                        Wrappers.<RoleDataScopeRelDO>lambdaQuery().eq(RoleDataScopeRelDO::getRoleId, roleId.value()))
                .stream()
                .map(RoleDataScopeRelDO::getDepartmentId)
                .map(DepartmentId::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Set.copyOf(departmentIds);
    }

    void updateDataScope(RoleId roleId, RoleDataScopeType dataScopeType, Collection<DepartmentId> departmentIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
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

    void delete(RoleId roleId) {
        BaconContextHolder.requireTenantId();
        roleDataScopeRelMapper.delete(
                Wrappers.<RoleDataScopeRelDO>lambdaQuery().eq(RoleDataScopeRelDO::getRoleId, roleId.value()));
        dataPermissionRuleMapper.delete(
                Wrappers.<DataPermissionRuleDO>lambdaQuery().eq(DataPermissionRuleDO::getRoleId, roleId.value()));
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
}
