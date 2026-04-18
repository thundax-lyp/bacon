package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
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
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(departmentMapper.selectOne(
                        Wrappers.<DepartmentDO>lambdaQuery().eq(DepartmentDO::getId, departmentId.value())))
                .map(DepartmentPersistenceAssembler::toDomain);
    }

    Optional<Department> findByCode(DepartmentCode code) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(departmentMapper.selectOne(
                        Wrappers.<DepartmentDO>lambdaQuery().eq(DepartmentDO::getCode, code.value())))
                .map(DepartmentPersistenceAssembler::toDomain);
    }

    List<Department> listByIds(Set<DepartmentId> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        BaconContextHolder.requireTenantId();
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

    List<Department> listTree() {
        BaconContextHolder.requireTenantId();
        return departmentMapper
                .selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .orderByAsc(DepartmentDO::getParentId, DepartmentDO::getSort, DepartmentDO::getId))
                .stream()
                .map(DepartmentPersistenceAssembler::toDomain)
                .toList();
    }

    Department insert(Department department) {
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(department);
        departmentMapper.insert(dataObject);
        return DepartmentPersistenceAssembler.toDomain(dataObject);
    }

    Department update(Department department) {
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(department);
        departmentMapper.updateById(dataObject);
        return DepartmentPersistenceAssembler.toDomain(dataObject);
    }

    Department updateSort(DepartmentId departmentId, Integer sort) {
        Department currentDepartment = findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
        currentDepartment.sort(sort);
        return update(currentDepartment);
    }

    void delete(DepartmentId departmentId) {
        BaconContextHolder.requireTenantId();
        departmentMapper.delete(Wrappers.<DepartmentDO>lambdaQuery().eq(DepartmentDO::getId, departmentId.value()));
    }

    boolean existsChild(DepartmentId departmentId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(departmentMapper.selectCount(Wrappers.<DepartmentDO>lambdaQuery()
                                .eq(DepartmentDO::getParentId, departmentId.value())))
                        .orElse(0L)
                > 0L;
    }
}
