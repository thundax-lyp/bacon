package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationResponse;
import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers/auth")
public class AuthProviderController {

    private final TokenVerifyFacade tokenVerifyFacade;
    private final SessionCommandFacade sessionCommandFacade;
    private final OAuthClientReadFacade oAuthClientReadFacade;

    public AuthProviderController(TokenVerifyFacade tokenVerifyFacade,
                                  SessionCommandFacade sessionCommandFacade,
                                  OAuthClientReadFacade oAuthClientReadFacade) {
        this.tokenVerifyFacade = tokenVerifyFacade;
        this.sessionCommandFacade = sessionCommandFacade;
        this.oAuthClientReadFacade = oAuthClientReadFacade;
    }

    @GetMapping("/tokens/verify")
    public SessionValidationResponse verify(@RequestParam("accessToken") String accessToken) {
        return tokenVerifyFacade.verifyAccessToken(accessToken);
    }

    @GetMapping("/sessions/{sessionId}")
    public CurrentSessionResponse currentSession(@PathVariable String sessionId) {
        return tokenVerifyFacade.getSessionContext(sessionId);
    }

    @PostMapping("/sessions/invalidate/user")
    public void invalidateUserSessions(@RequestParam("tenantId") Long tenantId,
                                       @RequestParam("userId") Long userId,
                                       @RequestParam("reason") String reason) {
        sessionCommandFacade.invalidateUserSessions(tenantId, userId, reason);
    }

    @PostMapping("/sessions/invalidate/tenant")
    public void invalidateTenantSessions(@RequestParam("tenantId") Long tenantId,
                                         @RequestParam("reason") String reason) {
        sessionCommandFacade.invalidateTenantSessions(tenantId, reason);
    }

    @PostMapping("/sessions/{sessionId}/invalidate")
    public void invalidateSession(@PathVariable String sessionId, @RequestParam("reason") String reason) {
        sessionCommandFacade.invalidateSession(sessionId, reason);
    }

    @GetMapping("/oauth-clients/{clientId}")
    public OAuthClientDTO getClient(@PathVariable String clientId) {
        return oAuthClientReadFacade.getClientByClientId(clientId);
    }
}
