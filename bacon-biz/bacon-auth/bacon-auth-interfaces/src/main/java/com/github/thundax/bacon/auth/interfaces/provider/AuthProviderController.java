package com.github.thundax.bacon.auth.interfaces.provider;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.application.command.SessionApplicationService;
import com.github.thundax.bacon.auth.application.command.TokenApplicationService;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.query.OAuth2ClientApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/providers/auth")
@Validated
@Tag(name = "Inner-Auth-Management", description = "Auth 域内部 Provider 接口")
public class AuthProviderController {

    private final TokenApplicationService tokenApplicationService;
    private final SessionApplicationService sessionApplicationService;
    private final OAuth2ClientApplicationService oAuth2ClientApplicationService;

    public AuthProviderController(
            TokenApplicationService tokenApplicationService,
            SessionApplicationService sessionApplicationService,
            OAuth2ClientApplicationService oAuth2ClientApplicationService) {
        this.tokenApplicationService = tokenApplicationService;
        this.sessionApplicationService = sessionApplicationService;
        this.oAuth2ClientApplicationService = oAuth2ClientApplicationService;
    }

    @Operation(summary = "校验访问令牌")
    @GetMapping("/tokens/verify")
    public SessionValidationDTO verify(
            @RequestParam("accessToken") @NotBlank(message = "accessToken must not be blank") String accessToken) {
        return tokenApplicationService.verifyAccessToken(accessToken);
    }

    @Operation(summary = "获取会话上下文")
    @GetMapping("/sessions/{sessionId}")
    public CurrentSessionFacadeResponse currentSession(
            @PathVariable @NotBlank(message = "sessionId must not be blank") String sessionId) {
        CurrentSessionDTO currentSession = tokenApplicationService.getSessionContext(sessionId);
        return CurrentSessionFacadeResponse.from(
                currentSession.getSessionId(),
                currentSession.getTenantId(),
                currentSession.getUserId(),
                currentSession.getIdentityType(),
                currentSession.getLoginType(),
                currentSession.getSessionStatus(),
                currentSession.getIssuedAt(),
                currentSession.getLastAccessTime(),
                currentSession.getExpireAt());
    }

    @Operation(summary = "失效指定用户会话")
    @PostMapping("/sessions/invalidate-user")
    public void invalidateUserSessions(
            @RequestParam("tenantId") @Positive(message = "tenantId must be greater than 0") Long tenantId,
            @RequestParam("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @RequestParam("reason") @NotBlank(message = "reason must not be blank") String reason) {
        sessionApplicationService.invalidateUserSessions(tenantId, userId, reason);
    }

    @Operation(summary = "失效指定租户会话")
    @PostMapping("/sessions/invalidate-tenant")
    public void invalidateTenantSessions(
            @RequestParam("tenantId") @Positive(message = "tenantId must be greater than 0") Long tenantId,
            @RequestParam("reason") @NotBlank(message = "reason must not be blank") String reason) {
        sessionApplicationService.invalidateTenantSessions(tenantId, reason);
    }

    @Operation(summary = "失效指定会话")
    @PostMapping("/sessions/{sessionId}/invalidate")
    public void invalidateSession(
            @PathVariable @NotBlank(message = "sessionId must not be blank") String sessionId,
            @RequestParam("reason") @NotBlank(message = "reason must not be blank") String reason) {
        sessionApplicationService.invalidateSession(sessionId, reason);
    }

    @Operation(summary = "查询 OAuth 客户端")
    @GetMapping("/oauth-clients/{clientId}")
    public OAuthClientDTO getClient(@PathVariable @NotBlank(message = "clientId must not be blank") String clientId) {
        return oAuth2ClientApplicationService.getClientByClientId(clientId);
    }
}
