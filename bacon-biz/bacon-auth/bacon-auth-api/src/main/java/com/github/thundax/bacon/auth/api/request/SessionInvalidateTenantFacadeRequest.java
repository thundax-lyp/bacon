package com.github.thundax.bacon.auth.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInvalidateTenantFacadeRequest {

    private Long tenantId;

    private String reason;
}
