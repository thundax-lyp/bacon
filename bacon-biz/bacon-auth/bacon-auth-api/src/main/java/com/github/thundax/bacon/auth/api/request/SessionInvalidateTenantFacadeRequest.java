package com.github.thundax.bacon.auth.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionInvalidateTenantFacadeRequest {

    private Long tenantId;

    private String reason;
}
