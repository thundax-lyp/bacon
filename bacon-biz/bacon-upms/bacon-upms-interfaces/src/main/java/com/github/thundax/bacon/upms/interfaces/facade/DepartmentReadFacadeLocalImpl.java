package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class DepartmentReadFacadeLocalImpl implements DepartmentReadFacade {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentReadFacadeLocalImpl(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @Override
    public DepartmentDTO getDepartmentById(String tenantNo, Long departmentId) {
        return departmentApplicationService.getDepartmentById(tenantNo, departmentId);
    }

    @Override
    public DepartmentDTO getDepartmentByCode(String tenantNo, String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(tenantNo, departmentCode);
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(String tenantNo, Set<Long> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(tenantNo, departmentIds);
    }
}
