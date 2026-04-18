package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.DepartmentPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DepartmentMapper;
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

    Optional<Department> findById(DepartmentId departmentId) {
        requireTenantId();
        return Optional.ofNullable(departmentMapper.selectOne(
                        Wrappers.<DepartmentDO>lambdaQuery().eq(DepartmentDO::getId, departmentId.value())))
                .map(DepartmentPersistenceAssembler::toDomain);
    }

    Optional<Department> findByCode(DepartmentCode code) {
        requireTenantId();
        return Optional.ofNullable(departmentMapper.selectOne(
                        Wrappers.<DepartmentDO>lambdaQuery().eq(DepartmentDO::getCode, code.value())))
                .map(DepartmentPersistenceAssembler::toDomain);
    }

    List<Department> listByIds(Set<DepartmentId> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        requireTenantId();
        return departmentMapper
                .selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .in(
                                DepartmentDO::getId,
                                departmentIds.stream().map(DepartmentId::value).toList())
                        .orderByAsc(DepartmentDO::getId))
                .stream()
                .map(DepartmentPersistenceAssembler::toDomain)
                .toList();
    }

    List<Department> listDepartmentTree() {
        requireTenantId();
        return departmentMapper
                .selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .orderByAsc(DepartmentDO::getParentId, DepartmentDO::getSort, DepartmentDO::getId))
                .stream()
                .map(DepartmentPersistenceAssembler::toDomain)
                .toList();
    }

    Department insertDepartment(Department department) {
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(department);
        departmentMapper.insert(dataObject);
        return DepartmentPersistenceAssembler.toDomain(dataObject);
    }

    Department updateDepartment(Department department) {
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(department);
        departmentMapper.updateById(dataObject);
        return DepartmentPersistenceAssembler.toDomain(dataObject);
    }

    Department updateDepartmentSort(DepartmentId departmentId, Integer sort) {
        Department currentDepartment = findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
        currentDepartment.sort(sort);
        return updateDepartment(currentDepartment);
    }

    void delete(DepartmentId departmentId) {
        requireTenantId();
        departmentMapper.delete(Wrappers.<DepartmentDO>lambdaQuery().eq(DepartmentDO::getId, departmentId.value()));
    }

    boolean existsChildDepartment(DepartmentId departmentId) {
        requireTenantId();
        return Optional.ofNullable(departmentMapper.selectCount(Wrappers.<DepartmentDO>lambdaQuery()
                                .eq(DepartmentDO::getParentId, departmentId.value())))
                        .orElse(0L)
                > 0L;
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
