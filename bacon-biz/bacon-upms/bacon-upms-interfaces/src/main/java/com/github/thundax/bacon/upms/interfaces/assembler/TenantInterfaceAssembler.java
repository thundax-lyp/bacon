package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.application.codec.TenantCodeCodec;
import com.github.thundax.bacon.upms.application.command.TenantCreateCommand;
import com.github.thundax.bacon.upms.application.command.TenantStatusUpdateCommand;
import com.github.thundax.bacon.upms.application.command.TenantUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.application.query.TenantPageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.interfaces.request.TenantCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.TenantPageRequest;
import com.github.thundax.bacon.upms.interfaces.request.TenantStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.TenantUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.TenantPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.TenantResponse;

public final class TenantInterfaceAssembler {

    private TenantInterfaceAssembler() {}

    public static TenantPageQuery toPageQuery(TenantPageRequest request) {
        return new TenantPageQuery(
                request.getName(),
                request.getStatus() == null ? null : TenantStatus.from(request.getStatus()),
                request.getPageNo(),
                request.getPageSize());
    }

    public static TenantCreateCommand toCreateCommand(TenantCreateRequest request) {
        return new TenantCreateCommand(request.name(), TenantCodeCodec.toDomain(request.code()), request.expiredAt());
    }

    public static TenantUpdateCommand toUpdateCommand(Long tenantId, TenantUpdateRequest request) {
        return new TenantUpdateCommand(
                TenantId.of(tenantId), request.name(), TenantCodeCodec.toDomain(request.code()), request.expiredAt());
    }

    public static TenantStatusUpdateCommand toStatusUpdateCommand(Long tenantId, TenantStatusUpdateRequest request) {
        return new TenantStatusUpdateCommand(
                TenantId.of(tenantId), request.status() == null ? null : TenantStatus.from(request.status()));
    }

    public static TenantResponse toResponse(TenantDTO dto) {
        return TenantResponse.from(dto);
    }

    public static TenantPageResponse toPageResponse(PageResult<TenantDTO> pageResult) {
        return TenantPageResponse.from(pageResult);
    }

    public static TenantFacadeResponse toFacadeResponse(TenantDTO dto) {
        return new TenantFacadeResponse(dto.getName(), dto.getCode(), dto.getStatus(), dto.getExpiredAt());
    }
}
