package com.github.thundax.bacon.auth.interfaces.provider;

import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.SessionInvalidateCommand;
import com.github.thundax.bacon.auth.application.command.SessionInvalidateTenantCommand;
import com.github.thundax.bacon.auth.application.command.SessionInvalidateUserCommand;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.query.OAuthClientQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.OAuthClientQuery;
import com.github.thundax.bacon.auth.application.query.SessionContextQuery;
import com.github.thundax.bacon.auth.application.query.TokenQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.TokenVerifyQuery;
import com.github.thundax.bacon.auth.interfaces.assembler.AuthInterfaceAssembler;
import com.github.thundax.bacon.auth.interfaces.response.CurrentSessionResponse;
import com.github.thundax.bacon.auth.interfaces.response.OAuthClientResponse;
import com.github.thundax.bacon.auth.interfaces.response.SessionValidationResponse;
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

    private final TokenQueryApplicationService tokenQueryApplicationService;
    private final SessionCommandApplicationService sessionCommandApplicationService;
    private final OAuthClientQueryApplicationService oAuthClientQueryApplicationService;

    public AuthProviderController(
            TokenQueryApplicationService tokenQueryApplicationService,
            SessionCommandApplicationService sessionCommandApplicationService,
            OAuthClientQueryApplicationService oAuthClientQueryApplicationService) {
        this.tokenQueryApplicationService = tokenQueryApplicationService;
        this.sessionCommandApplicationService = sessionCommandApplicationService;
        this.oAuthClientQueryApplicationService = oAuthClientQueryApplicationService;
    }

    @Operation(summary = "校验访问令牌")
    @GetMapping("/tokens/verify")
    public SessionValidationResponse verify(
            @RequestParam("accessToken") @NotBlank(message = "accessToken must not be blank") String accessToken) {
        return AuthInterfaceAssembler.toSessionValidationResponse(
                tokenQueryApplicationService.verifyAccessToken(new TokenVerifyQuery(accessToken)));
    }

    @Operation(summary = "获取会话上下文")
    @GetMapping("/sessions/{sessionId}")
    public CurrentSessionResponse currentSession(
            @PathVariable @NotBlank(message = "sessionId must not be blank") String sessionId) {
        CurrentSessionDTO currentSession = tokenQueryApplicationService.getSessionContext(new SessionContextQuery(sessionId));
        return AuthInterfaceAssembler.toCurrentSessionResponse(currentSession);
    }

    @Operation(summary = "失效指定用户会话")
    @PostMapping("/sessions/invalidate-user")
    public void invalidateUserSessions(
            @RequestParam("tenantId") @Positive(message = "tenantId must be greater than 0") Long tenantId,
            @RequestParam("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @RequestParam("reason") @NotBlank(message = "reason must not be blank") String reason) {
        sessionCommandApplicationService.invalidateUserSessions(new SessionInvalidateUserCommand(tenantId, userId, reason));
    }

    @Operation(summary = "失效指定租户会话")
    @PostMapping("/sessions/invalidate-tenant")
    public void invalidateTenantSessions(
            @RequestParam("tenantId") @Positive(message = "tenantId must be greater than 0") Long tenantId,
            @RequestParam("reason") @NotBlank(message = "reason must not be blank") String reason) {
        sessionCommandApplicationService.invalidateTenantSessions(new SessionInvalidateTenantCommand(tenantId, reason));
    }

    @Operation(summary = "失效指定会话")
    @PostMapping("/sessions/{sessionId}/invalidate")
    public void invalidateSession(
            @PathVariable @NotBlank(message = "sessionId must not be blank") String sessionId,
            @RequestParam("reason") @NotBlank(message = "reason must not be blank") String reason) {
        sessionCommandApplicationService.invalidateSession(new SessionInvalidateCommand(sessionId, reason));
    }

    @Operation(summary = "查询 OAuth 客户端")
    @GetMapping("/oauth-clients/{clientId}")
    public OAuthClientResponse getClient(@PathVariable @NotBlank(message = "clientId must not be blank") String clientId) {
        return AuthInterfaceAssembler.toOAuthClientResponse(
                oAuthClientQueryApplicationService.getClientByClientId(new OAuthClientQuery(clientId)));
    }
}
