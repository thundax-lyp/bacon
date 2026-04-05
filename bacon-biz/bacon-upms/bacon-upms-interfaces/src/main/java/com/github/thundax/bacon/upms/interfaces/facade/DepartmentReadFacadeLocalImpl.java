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
import java.util.stream.Collectors;

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
    public DepartmentDTO getDepartmentById(String tenantId, String departmentId) {
        return departmentApplicationService.getDepartmentById(requireExistingTenantId(tenantId), toDepartmentId(departmentId));
    }

    @Override
    public DepartmentDTO getDepartmentByCode(String tenantId, String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(requireExistingTenantId(tenantId), departmentCode);
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(String tenantId, Set<String> departmentIds) {
        Set<DepartmentId> resolvedDepartmentIds = departmentIds == null ? Set.of() : departmentIds.stream()
                .map(this::toDepartmentId)
                .collect(Collectors.toSet());
        return departmentApplicationService.listDepartmentsByIds(requireExistingTenantId(tenantId), resolvedDepartmentIds);
    }

    private TenantId requireExistingTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        return tenantApplicationService.getTenantByTenantId(tenantId.trim()).getId();
    }

    private DepartmentId toDepartmentId(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            throw new IllegalArgumentException("departmentId must not be blank");
        }
        return DepartmentId.of(Long.parseLong(departmentId.trim()));
    }
}
