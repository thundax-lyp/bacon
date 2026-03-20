package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.application.service.DepartmentApplicationService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DepartmentReadFacadeLocalImpl implements DepartmentReadFacade {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentReadFacadeLocalImpl(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @Override
    public DepartmentDTO getDepartmentById(Long tenantId, Long departmentId) {
        return departmentApplicationService.getDepartmentById(tenantId, departmentId);
    }

    @Override
    public DepartmentDTO getDepartmentByCode(Long tenantId, String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(tenantId, departmentCode);
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(tenantId, departmentIds);
    }
}
