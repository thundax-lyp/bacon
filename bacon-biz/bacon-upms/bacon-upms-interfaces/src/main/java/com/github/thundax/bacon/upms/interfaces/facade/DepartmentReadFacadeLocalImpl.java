package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.application.command.TenantApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class DepartmentReadFacadeLocalImpl implements DepartmentReadFacade {

    private final DepartmentApplicationService departmentApplicationService;
    private final TenantApplicationService tenantApplicationService;

    public DepartmentReadFacadeLocalImpl(DepartmentApplicationService departmentApplicationService,
                                         TenantApplicationService tenantApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
        this.tenantApplicationService = tenantApplicationService;
    }

    @Override
    public DepartmentDTO getDepartmentById(@NonNull TenantId tenantId, @NonNull DepartmentId departmentId) {
        return departmentApplicationService.getDepartmentById(resolveExistingTenantId(tenantId), departmentId);
    }

    @Override
    public DepartmentDTO getDepartmentByCode(@NonNull TenantId tenantId, String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(resolveExistingTenantId(tenantId), departmentCode);
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(@NonNull TenantId tenantId, @NonNull Set<DepartmentId> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(resolveExistingTenantId(tenantId), Set.copyOf(departmentIds));
    }

    private TenantId resolveExistingTenantId(TenantId tenantId) {
        return tenantApplicationService.getTenantByTenantId(tenantId.value()).getId();
    }
}
