package com.github.thundax.bacon.auth.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInvalidateUserFacadeRequest {

    private Long tenantId;

    private Long userId;

    private String reason;
}
