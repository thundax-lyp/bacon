package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DepartmentMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
class DepartmentPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final DepartmentMapper departmentMapper;

    DepartmentPersistenceSupport(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    Optional<Department> findDepartmentById(Long tenantId, Long departmentId) {
        return Optional.ofNullable(departmentMapper.selectOne(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getId, departmentId)))
                .map(this::toDomain);
    }

    Optional<Department> findDepartmentByCode(Long tenantId, String code) {
        return Optional.ofNullable(departmentMapper.selectOne(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getCode, code)))
                .map(this::toDomain);
    }

    List<Department> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        return departmentMapper.selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .in(DepartmentDO::getId, departmentIds)
                        .orderByAsc(DepartmentDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    List<Department> listDepartmentTree(Long tenantId) {
        return departmentMapper.selectList(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .orderByAsc(DepartmentDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    Department saveDepartment(Department department) {
        DepartmentDO dataObject = toDataObject(department);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            departmentMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            departmentMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    void deleteDepartment(Long tenantId, Long departmentId) {
        departmentMapper.delete(Wrappers.<DepartmentDO>lambdaQuery()
                .eq(DepartmentDO::getTenantId, tenantId)
                .eq(DepartmentDO::getId, departmentId));
    }

    boolean existsChildDepartment(Long tenantId, Long departmentId) {
        return Optional.ofNullable(departmentMapper.selectCount(Wrappers.<DepartmentDO>lambdaQuery()
                        .eq(DepartmentDO::getTenantId, tenantId)
                        .eq(DepartmentDO::getParentId, departmentId)))
                .orElse(0L) > 0L;
    }
}
