package com.github.thundax.bacon.auth.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 授权确认请求")
public class OAuth2DecisionRequest {

    @Schema(description = "授权请求ID", example = "auth-req-001")
    @NotBlank(message = "authorizationRequestId: must not be blank")
    private String authorizationRequestId;

    @Schema(description = "授权决策", example = "APPROVE")
    @NotBlank(message = "decision: must not be blank")
    @Pattern(regexp = "APPROVE|REJECT", message = "decision: must be APPROVE or REJECT")
    private String decision;
}
