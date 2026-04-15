package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class DepartmentReadFacadeLocalImpl implements DepartmentReadFacade {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentReadFacadeLocalImpl(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @Override
    public DepartmentDTO getDepartmentById(@NonNull DepartmentId departmentId) {
        requireContext();
        return departmentApplicationService.getDepartmentById(departmentId);
    }

    @Override
    public DepartmentDTO getDepartmentByCode(String departmentCode) {
        requireContext();
        return departmentApplicationService.getDepartmentByCode(DepartmentCode.of(departmentCode));
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(@NonNull Set<DepartmentId> departmentIds) {
        requireContext();
        return departmentApplicationService.listDepartmentsByIds(Set.copyOf(departmentIds));
    }

    private void requireContext() {
        BaconContextHolder.requireTenantId();
    }
}
