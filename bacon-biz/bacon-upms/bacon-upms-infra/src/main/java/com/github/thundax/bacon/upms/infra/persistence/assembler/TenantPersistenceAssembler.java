package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.TenantDO;
import com.github.thundax.bacon.common.id.domain.TenantId;

public final class TenantPersistenceAssembler {

    private TenantPersistenceAssembler() {}

    public static TenantDO toDataObject(Tenant tenant) {
        return new TenantDO(
                tenant.getId() == null ? null : tenant.getId().value(),
                tenant.getTenantCode().value(),
                tenant.getName(),
                tenant.getStatus().value(),
                tenant.getExpiredAt());
    }

    public static Tenant toDomain(TenantDO dataObject) {
        return Tenant.reconstruct(
                dataObject.getId() == null ? null : TenantId.of(dataObject.getId()),
                dataObject.getName(),
                TenantCode.of(dataObject.getCode()),
                TenantStatus.from(dataObject.getStatus()),
                dataObject.getExpiredAt());
    }
}
