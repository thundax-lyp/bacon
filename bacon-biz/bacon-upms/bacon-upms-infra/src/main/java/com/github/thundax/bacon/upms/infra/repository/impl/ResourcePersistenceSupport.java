package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
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

    Optional<Resource> findResourceById(TenantId tenantId, ResourceId resourceId) {
        return Optional.ofNullable(resourceMapper.selectOne(Wrappers.<ResourceDO>lambdaQuery()
                        .eq(ResourceDO::getTenantId, tenantId)
                        .eq(ResourceDO::getId, resourceId)))
                .map(this::toDomain);
    }

    List<Resource> listResources(String code, String name, String resourceType, String status, int pageNo, int pageSize) {
        return resourceMapper
                .selectList(Wrappers.<ResourceDO>lambdaQuery()
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

    long countResources(String code, String name, String resourceType, String status) {
        return Optional.ofNullable(resourceMapper.selectCount(Wrappers.<ResourceDO>lambdaQuery()
                        .like(hasText(code), ResourceDO::getCode, code)
                        .like(hasText(name), ResourceDO::getName, name)
                        .eq(hasText(resourceType), ResourceDO::getResourceType, trim(resourceType))
                        .eq(hasText(status), ResourceDO::getStatus, trim(status))))
                .orElse(0L);
    }

    Resource saveResource(Resource resource) {
        ResourceDO dataObject = toDataObject(resource);
        if (dataObject.getId() == null || resourceMapper.selectById(dataObject.getId()) == null) {
            resourceMapper.insert(dataObject);
        } else {
            resourceMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    void deleteResource(TenantId tenantId, ResourceId resourceId) {
        resourceMapper.delete(Wrappers.<ResourceDO>lambdaQuery()
                .eq(ResourceDO::getTenantId, tenantId)
                .eq(ResourceDO::getId, resourceId));
        roleResourceRelMapper.delete(Wrappers.<RoleResourceRelDO>lambdaQuery()
                .eq(RoleResourceRelDO::getTenantId, tenantId)
                .eq(RoleResourceRelDO::getResourceId, resourceId));
    }
}
