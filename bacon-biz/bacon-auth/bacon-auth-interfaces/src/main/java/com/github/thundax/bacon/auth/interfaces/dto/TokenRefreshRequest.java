package com.github.thundax.bacon.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌请求")
public class TokenRefreshRequest {

    @Schema(description = "刷新令牌", example = "refresh-token-demo")
    @NotBlank(message = "refreshToken: must not be blank")
    private String refreshToken;
}
