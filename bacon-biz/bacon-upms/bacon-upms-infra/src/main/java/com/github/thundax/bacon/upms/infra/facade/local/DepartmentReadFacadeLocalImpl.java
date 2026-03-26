package com.github.thundax.bacon.upms.infra.facade.local;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.application.service.DepartmentApplicationService;
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
