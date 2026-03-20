package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationResponse;
import com.github.thundax.bacon.auth.application.service.OAuth2ClientApplicationService;
import com.github.thundax.bacon.auth.application.service.SessionApplicationService;
import com.github.thundax.bacon.auth.application.service.TokenApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers/auth")
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

    @GetMapping("/tokens/verify")
    public SessionValidationResponse verify(@RequestParam("accessToken") String accessToken) {
        return tokenApplicationService.verifyAccessToken(accessToken);
    }

    @GetMapping("/sessions/{sessionId}")
    public CurrentSessionResponse currentSession(@PathVariable String sessionId) {
        return tokenApplicationService.getSessionContext(sessionId);
    }

    @PostMapping("/sessions/invalidate/user")
    public void invalidateUserSessions(@RequestParam("tenantId") Long tenantId,
                                       @RequestParam("userId") Long userId,
                                       @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateUserSessions(tenantId, userId, reason);
    }

    @PostMapping("/sessions/invalidate/tenant")
    public void invalidateTenantSessions(@RequestParam("tenantId") Long tenantId,
                                         @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateTenantSessions(tenantId, reason);
    }

    @PostMapping("/sessions/{sessionId}/invalidate")
    public void invalidateSession(@PathVariable String sessionId, @RequestParam("reason") String reason) {
        sessionApplicationService.invalidateSession(sessionId, reason);
    }

    @GetMapping("/oauth-clients/{clientId}")
    public OAuthClientDTO getClient(@PathVariable String clientId) {
        return oAuth2ClientApplicationService.getClientByClientId(clientId);
    }
}
