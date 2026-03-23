package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationResponse;
import com.github.thundax.bacon.auth.application.service.OAuth2ClientApplicationService;
import com.github.thundax.bacon.auth.application.service.SessionApplicationService;
import com.github.thundax.bacon.auth.application.service.TokenApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/providers/auth")
@Tag(name = "Auth Provider", description = "Auth 域内部 Provider 接口")
public class AuthProviderController {

    private final TokenApplicationService tokenApplicationService;
    private final SessionApplicationService sessionApplicationService;
    private final OAuth2ClientApplicationService oAuth2ClientApplicationService;

    public AuthProviderController(TokenApplicationService tokenApplicationService,
                                  SessionApplicationService sessionApplicationService,
                                  OAuth2ClientApplicationService oAuth2ClientApplicationService) {
        this.tokenApplicationService = tokenApplicationService;
        this.sessionApplicationService = sessionApplicationService;
        this.oAuth2ClientApplicationService = oAuth2ClientApplicationService;
    }

    @Operation(summary = "校验访问令牌")
    @GetMapping("/tokens/verify")
    public SessionValidationResponse verify(@RequestParam("accessToken") String accessToken) {
        return tokenApplicationService.verifyAccessToken(accessToken);
    }

    @Operation(summary = "获取会话上下文")
    @GetMapping("/sessions/{sessionId}")
    public CurrentSessionResponse currentSession(@PathVariable String sessionId) {
        return tokenApplicationService.getSessionContext(sessionId);
    }

    @Operation(summary = "失效指定用户会话")
    @PostMapping("/sessions/invalidate/user")
    public void invalidateUserSessions(@RequestParam("tenantId") Long tenantId,
                                       @RequestParam("userId") Long userId,
                                       @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateUserSessions(tenantId, userId, reason);
    }

    @Operation(summary = "失效指定租户会话")
    @PostMapping("/sessions/invalidate/tenant")
    public void invalidateTenantSessions(@RequestParam("tenantId") Long tenantId,
                                         @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateTenantSessions(tenantId, reason);
    }

    @Operation(summary = "失效指定会话")
    @PostMapping("/sessions/{sessionId}/invalidate")
    public void invalidateSession(@PathVariable String sessionId, @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateSession(sessionId, reason);
    }

    @Operation(summary = "查询 OAuth 客户端")
    @GetMapping("/oauth-clients/{clientId}")
    public OAuthClientDTO getClient(@PathVariable String clientId) {
        return oAuth2ClientApplicationService.getClientByClientId(clientId);
    }
}
