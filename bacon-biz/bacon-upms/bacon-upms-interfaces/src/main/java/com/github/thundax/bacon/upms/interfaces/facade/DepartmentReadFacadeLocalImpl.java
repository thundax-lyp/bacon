package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.api.request.DepartmentCodeGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.DepartmentGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.DepartmentListFacadeRequest;
import com.github.thundax.bacon.upms.api.response.DepartmentFacadeResponse;
import com.github.thundax.bacon.upms.api.response.DepartmentListFacadeResponse;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class DepartmentReadFacadeLocalImpl implements DepartmentReadFacade {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentReadFacadeLocalImpl(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @Override
    public DepartmentFacadeResponse getDepartmentById(DepartmentGetFacadeRequest request) {
        requireContext();
        return DepartmentFacadeResponse.from(
                departmentApplicationService.getDepartmentById(DepartmentId.of(request.getDepartmentId())));
    }

    @Override
    public DepartmentFacadeResponse getDepartmentByCode(DepartmentCodeGetFacadeRequest request) {
        requireContext();
        return DepartmentFacadeResponse.from(
                departmentApplicationService.getDepartmentByCode(DepartmentCode.of(request.getDepartmentCode())));
    }

    @Override
    public DepartmentListFacadeResponse listDepartmentsByIds(DepartmentListFacadeRequest request) {
        requireContext();
        Set<DepartmentId> departmentIds =
                request.getDepartmentIds().stream().map(DepartmentId::of).collect(Collectors.toUnmodifiableSet());
        return DepartmentListFacadeResponse.from(departmentApplicationService.listDepartmentsByIds(departmentIds));
    }

    private void requireContext() {
        BaconContextHolder.requireTenantId();
    }
}
