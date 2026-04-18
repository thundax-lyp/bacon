package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.infra.persistence.assembler.ResourcePersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleResourceRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.ResourceMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleResourceRelMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class ResourcePersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final ResourceMapper resourceMapper;
    private final RoleResourceRelMapper roleResourceRelMapper;

    ResourcePersistenceSupport(ResourceMapper resourceMapper, RoleResourceRelMapper roleResourceRelMapper) {
        this.resourceMapper = resourceMapper;
        this.roleResourceRelMapper = roleResourceRelMapper;
    }

    Optional<Resource> findResourceById(ResourceId resourceId) {
        requireTenantId();
        return Optional.ofNullable(resourceMapper.selectOne(
                        Wrappers.<ResourceDO>lambdaQuery().eq(ResourceDO::getId, resourceId.value())))
                .map(ResourcePersistenceAssembler::toDomain);
    }

    List<Resource> listResources(
            ResourceCode code, String name, ResourceType resourceType, ResourceStatus status, int pageNo, int pageSize) {
        return resourceMapper
                .selectList(Wrappers.<ResourceDO>lambdaQuery()
                        .like(code != null, ResourceDO::getCode, code == null ? null : code.value())
                        .like(hasText(name), ResourceDO::getName, name)
                        .eq(resourceType != null, ResourceDO::getResourceType, resourceType.value())
                        .eq(status != null, ResourceDO::getStatus, status.value())
                        .orderByAsc(ResourceDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(ResourcePersistenceAssembler::toDomain)
                .toList();
    }

    long countResources(ResourceCode code, String name, ResourceType resourceType, ResourceStatus status) {
        return Optional.ofNullable(resourceMapper.selectCount(Wrappers.<ResourceDO>lambdaQuery()
                        .like(code != null, ResourceDO::getCode, code == null ? null : code.value())
                        .like(hasText(name), ResourceDO::getName, name)
                        .eq(resourceType != null, ResourceDO::getResourceType, resourceType.value())
                        .eq(status != null, ResourceDO::getStatus, status.value())))
                .orElse(0L);
    }

    Resource insertResource(Resource resource) {
        ResourceDO dataObject = ResourcePersistenceAssembler.toDataObject(resource);
        resourceMapper.insert(dataObject);
        return ResourcePersistenceAssembler.toDomain(dataObject);
    }

    Resource updateResource(Resource resource) {
        ResourceDO dataObject = ResourcePersistenceAssembler.toDataObject(resource);
        resourceMapper.updateById(dataObject);
        return ResourcePersistenceAssembler.toDomain(dataObject);
    }

    void deleteResource(ResourceId resourceId) {
        requireTenantId();
        resourceMapper.delete(Wrappers.<ResourceDO>lambdaQuery().eq(ResourceDO::getId, resourceId.value()));
        roleResourceRelMapper.delete(
                Wrappers.<RoleResourceRelDO>lambdaQuery().eq(RoleResourceRelDO::getResourceId, resourceId.value()));
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
