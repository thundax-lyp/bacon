package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.infra.persistence.assembler.TenantPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.TenantDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.TenantMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class TenantPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final TenantMapper tenantMapper;

    TenantPersistenceSupport(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    Optional<Tenant> findById(TenantId tenantId) {
        return Optional.ofNullable(tenantId).map(tenantMapper::selectById).map(TenantPersistenceAssembler::toDomain);
    }

    Optional<Tenant> findTenantByTenantId(TenantId tenantId) {
        return Optional.ofNullable(
                        tenantMapper.selectOne(Wrappers.<TenantDO>lambdaQuery().eq(TenantDO::getId, tenantId)))
                .map(TenantPersistenceAssembler::toDomain);
    }

    Optional<Tenant> findByCode(String code) {
        return Optional.ofNullable(tenantMapper.selectOne(
                        Wrappers.<TenantDO>lambdaQuery().eq(TenantDO::getCode, trim(code))))
                .map(TenantPersistenceAssembler::toDomain);
    }

    List<Tenant> listTenants(String name, String status, int pageNo, int pageSize) {
        return tenantMapper
                .selectList(Wrappers.<TenantDO>lambdaQuery()
                        .like(hasText(name), TenantDO::getName, name)
                        .eq(hasText(status), TenantDO::getStatus, trim(status))
                        .orderByAsc(TenantDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(TenantPersistenceAssembler::toDomain)
                .toList();
    }

    long count(String name, String status) {
        return Optional.ofNullable(tenantMapper.selectCount(Wrappers.<TenantDO>lambdaQuery()
                        .like(hasText(name), TenantDO::getName, name)
                        .eq(hasText(status), TenantDO::getStatus, trim(status))))
                .orElse(0L);
    }

    Tenant saveTenant(Tenant tenant) {
        TenantDO tenantDO = TenantPersistenceAssembler.toDataObject(tenant);
        if (tenantDO.getId() == null) {
            tenantMapper.insert(tenantDO);
        } else {
            tenantMapper.updateById(tenantDO);
        }
        return TenantPersistenceAssembler.toDomain(tenantDO);
    }
}
