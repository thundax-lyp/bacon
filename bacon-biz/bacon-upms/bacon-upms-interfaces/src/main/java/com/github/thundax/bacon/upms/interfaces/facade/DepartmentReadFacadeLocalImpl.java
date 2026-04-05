package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.application.command.TenantApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    public DepartmentDTO getDepartmentById(Long tenantId, Long departmentId) {
        return departmentApplicationService.getDepartmentById(requireExistingTenantId(tenantId), toDepartmentId(departmentId));
    }

    @Override
    public DepartmentDTO getDepartmentByCode(Long tenantId, String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(requireExistingTenantId(tenantId), departmentCode);
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        Set<DepartmentId> resolvedDepartmentIds = departmentIds == null ? Set.of() : departmentIds.stream()
                .map(this::toDepartmentId)
                .collect(java.util.stream.Collectors.toSet());
        return departmentApplicationService.listDepartmentsByIds(requireExistingTenantId(tenantId), resolvedDepartmentIds);
    }

    private TenantId requireExistingTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        return tenantApplicationService.getTenantByTenantId(TenantId.of(tenantId)).getId();
    }

    private DepartmentId toDepartmentId(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("departmentId must not be null");
        }
        return DepartmentId.of(departmentId);
    }
}
