package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantNo;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.TenantDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.TenantMapper;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
class TenantPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final TenantMapper tenantMapper;

    TenantPersistenceSupport(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    Optional<Tenant> findTenantById(Long tenantId) {
        return Optional.ofNullable(tenantId)
                .map(tenantMapper::selectById)
                .map(this::toDomain);
    }

    Optional<Tenant> findTenantByTenantNo(TenantNo tenantNo) {
        String tenantNoValue = tenantNo == null ? null : tenantNo.value();
        return Optional.ofNullable(tenantMapper.selectOne(Wrappers.<TenantDO>lambdaQuery()
                        .eq(TenantDO::getTenantNo, trim(tenantNoValue))))
                .map(this::toDomain);
    }

    List<Tenant> listTenants(TenantNo tenantNo, String name, String status, int pageNo, int pageSize) {
        String tenantNoValue = tenantNo == null ? null : tenantNo.value();
        return tenantMapper.selectList(Wrappers.<TenantDO>lambdaQuery()
                        .eq(hasText(tenantNoValue), TenantDO::getTenantNo, trim(tenantNoValue))
                        .like(hasText(name), TenantDO::getName, name)
                        .eq(hasText(status), TenantDO::getStatus, trim(status))
                        .orderByAsc(TenantDO::getTenantNo)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    long countTenants(TenantNo tenantNo, String name, String status) {
        String tenantNoValue = tenantNo == null ? null : tenantNo.value();
        return Optional.ofNullable(tenantMapper.selectCount(Wrappers.<TenantDO>lambdaQuery()
                        .eq(hasText(tenantNoValue), TenantDO::getTenantNo, trim(tenantNoValue))
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
