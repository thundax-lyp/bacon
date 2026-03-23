package com.github.thundax.bacon.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 授权确认请求")
public class OAuth2DecisionRequest {

    @Schema(description = "授权请求ID", example = "auth-req-001")
    private String authorizationRequestId;

    @Schema(description = "授权决策", example = "APPROVE")
    private String decision;
}
