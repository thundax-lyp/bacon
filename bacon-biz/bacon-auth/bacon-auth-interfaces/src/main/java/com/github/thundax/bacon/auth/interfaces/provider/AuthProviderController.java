package com.github.thundax.bacon.auth.interfaces.provider;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.application.query.OAuth2ClientApplicationService;
import com.github.thundax.bacon.auth.application.command.SessionApplicationService;
import com.github.thundax.bacon.auth.application.command.TokenApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers/auth")
@Tag(name = "Inner-Auth-Management", description = "Auth 域内部 Provider 接口")
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
    public SessionValidationDTO verify(@RequestParam("accessToken") String accessToken) {
        return tokenApplicationService.verifyAccessToken(accessToken);
    }

    @Operation(summary = "获取会话上下文")
    @GetMapping("/sessions/{sessionId}")
    public CurrentSessionDTO currentSession(@PathVariable String sessionId) {
        return tokenApplicationService.getSessionContext(sessionId);
    }

    @Operation(summary = "失效指定用户会话")
    @PostMapping("/sessions/invalidate/user")
    public void invalidateUserSessions(@RequestParam("tenantNo") String tenantNo,
                                       @RequestParam("userId") String userId,
                                       @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateUserSessions(tenantNo, userId, reason);
    }

    @Operation(summary = "失效指定租户会话")
    @PostMapping("/sessions/invalidate/tenant")
    public void invalidateTenantSessions(@RequestParam("tenantNo") String tenantNo,
                                         @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateTenantSessions(tenantNo, reason);
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
