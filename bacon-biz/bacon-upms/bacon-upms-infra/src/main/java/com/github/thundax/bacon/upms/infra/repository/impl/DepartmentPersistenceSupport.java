package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DepartmentMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class DepartmentPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final DepartmentMapper departmentMapper;

    DepartmentPersistenceSupport(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    Optional<Department> findDepartmentById(TenantId tenantId, DepartmentId departmentId) {
        return Optional.ofNullable(departmentMapper.selectOne(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getId, departmentId)))
                .map(this::toDomain);
    }

    Optional<Department> findDepartmentByCode(TenantId tenantId, String code) {
        return Optional.ofNullable(departmentMapper.selectOne(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getCode, code)))
                .map(this::toDomain);
    }

    List<Department> listDepartmentsByIds(TenantId tenantId, Set<DepartmentId> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        return departmentMapper
                .selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .in(DepartmentDO::getId, departmentIds)
                        .orderByAsc(DepartmentDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    List<Department> listDepartmentTree(TenantId tenantId) {
        return departmentMapper
                .selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .orderByAsc(DepartmentDO::getParentId, DepartmentDO::getSort, DepartmentDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    Department saveDepartment(Department department) {
        DepartmentDO dataObject = toDataObject(department);
        LocalDateTime now = LocalDateTime.now();
        DepartmentId departmentId = dataObject.getId();
        boolean exists = departmentId != null
                && departmentMapper.selectOne(Wrappers.<DepartmentDO>lambdaQuery()
                                .eq(DepartmentDO::getTenantId, dataObject.getTenantId())
                                .eq(DepartmentDO::getId, departmentId))
                        != null;
        if (!exists) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            departmentMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            departmentMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    Department updateDepartmentSort(TenantId tenantId, DepartmentId departmentId, Integer sort) {
        Department currentDepartment = findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        return saveDepartment(Department.reconstruct(
                currentDepartment.getId(),
                currentDepartment.getTenantId(),
                currentDepartment.getCode(),
                currentDepartment.getName(),
                currentDepartment.getParentId(),
                currentDepartment.getLeaderUserId(),
                sort,
                currentDepartment.getStatus(),
                currentDepartment.getCreatedBy(),
                currentDepartment.getCreatedAt(),
                currentDepartment.getUpdatedBy(),
                currentDepartment.getUpdatedAt()));
    }

    void deleteDepartment(TenantId tenantId, DepartmentId departmentId) {
        departmentMapper.delete(Wrappers.<DepartmentDO>lambdaQuery()
                .eq(DepartmentDO::getTenantId, tenantId)
                .eq(DepartmentDO::getId, departmentId));
    }

    boolean existsChildDepartment(TenantId tenantId, DepartmentId departmentId) {
        return Optional.ofNullable(departmentMapper.selectCount(Wrappers.<DepartmentDO>lambdaQuery()
                                .eq(DepartmentDO::getTenantId, tenantId)
                                .eq(DepartmentDO::getParentId, departmentId)))
                        .orElse(0L)
                > 0L;
    }
}
