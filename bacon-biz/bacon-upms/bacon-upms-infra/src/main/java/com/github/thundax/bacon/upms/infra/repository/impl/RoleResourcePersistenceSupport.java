package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleResourceRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.ResourceMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleResourceRelMapper;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class RoleResourcePersistenceSupport extends AbstractUpmsPersistenceSupport {

    private static final String ROLE_RESOURCE_REL_ID_BIZ_TAG = "upms-role-resource-rel-id";

    private final ResourceMapper resourceMapper;
    private final RoleResourceRelMapper roleResourceRelMapper;
    private final IdGenerator idGenerator;

    RoleResourcePersistenceSupport(
            ResourceMapper resourceMapper, RoleResourceRelMapper roleResourceRelMapper, IdGenerator idGenerator) {
        this.resourceMapper = resourceMapper;
        this.roleResourceRelMapper = roleResourceRelMapper;
        this.idGenerator = idGenerator;
    }

    Set<ResourceCode> findResourceCodes(RoleId roleId) {
        BaconContextHolder.requireTenantId();
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
                .map(ResourceCode::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    void updateResourceCodes(RoleId roleId, Collection<ResourceCode> resourceCodes) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        roleResourceRelMapper.delete(
                Wrappers.<RoleResourceRelDO>lambdaQuery().eq(RoleResourceRelDO::getRoleId, roleId.value()));
        if (resourceCodes == null || resourceCodes.isEmpty()) {
            return;
        }
        List<ResourceDO> resources = resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery().in(
                        ResourceDO::getCode,
                        new LinkedHashSet<>(resourceCodes).stream().map(ResourceCode::value).toList()));
        for (ResourceDO resource : resources) {
            roleResourceRelMapper.insert(new RoleResourceRelDO(
                    idGenerator.nextId(ROLE_RESOURCE_REL_ID_BIZ_TAG),
                    tenantId.value(),
                    roleId.value(),
                    resource.getId()));
        }
    }
}
