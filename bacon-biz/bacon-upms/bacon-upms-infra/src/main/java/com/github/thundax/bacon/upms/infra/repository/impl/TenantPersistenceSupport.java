package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.TenantDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.TenantMapper;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class TenantPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final TenantMapper tenantMapper;

    TenantPersistenceSupport(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    Optional<Tenant> findTenantById(TenantId tenantId) {
        return Optional.ofNullable(tenantId)
                .map(tenantMapper::selectById)
                .map(this::toDomain);
    }

    Optional<Tenant> findTenantByTenantId(TenantId tenantId) {
        return Optional.ofNullable(tenantMapper.selectOne(Wrappers.<TenantDO>lambdaQuery()
                        .eq(TenantDO::getId, tenantId)))
                .map(this::toDomain);
    }

    Optional<Tenant> findTenantByCode(String tenantCode) {
        return Optional.ofNullable(tenantMapper.selectOne(Wrappers.<TenantDO>lambdaQuery()
                        .eq(TenantDO::getCode, trim(tenantCode))))
                .map(this::toDomain);
    }

    List<Tenant> listTenants(TenantId tenantId, String name, String status, int pageNo, int pageSize) {
        return tenantMapper.selectList(Wrappers.<TenantDO>lambdaQuery()
                        .eq(tenantId != null, TenantDO::getId, tenantId)
                        .like(hasText(name), TenantDO::getName, name)
                        .eq(hasText(status), TenantDO::getStatus, trim(status))
                        .orderByAsc(TenantDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    long countTenants(TenantId tenantId, String name, String status) {
        return Optional.ofNullable(tenantMapper.selectCount(Wrappers.<TenantDO>lambdaQuery()
                        .eq(tenantId != null, TenantDO::getId, tenantId)
                        .like(hasText(name), TenantDO::getName, name)
                        .eq(hasText(status), TenantDO::getStatus, trim(status))))
                .orElse(0L);
    }

    Tenant saveTenant(Tenant tenant) {
        TenantDO tenantDO = toDataObject(tenant);
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.Clock.systemUTC());
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
}
