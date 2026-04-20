package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantFacadeResponse {

    private TenantDTO tenant;

    public static TenantFacadeResponse from(TenantDTO tenant) {
        return new TenantFacadeResponse(tenant);
    }
}
