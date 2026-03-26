package com.github.thundax.bacon.auth.infra.facade.remote;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class SessionCommandFacadeRemoteImpl implements SessionCommandFacade {

    private final RestClient restClient;

    public SessionCommandFacadeRemoteImpl(@Value("${bacon.remote.auth-base-url:http://127.0.0.1:8081/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void invalidateUserSessions(Long tenantId, Long userId, String reason) {
        restClient.post()
                .uri("/providers/auth/sessions/invalidate/user?tenantId={tenantId}&userId={userId}&reason={reason}",
                        tenantId, userId, reason)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void invalidateTenantSessions(Long tenantId, String reason) {
        restClient.post()
                .uri("/providers/auth/sessions/invalidate/tenant?tenantId={tenantId}&reason={reason}", tenantId, reason)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void invalidateSession(String sessionId, String reason) {
        restClient.post()
                .uri("/providers/auth/sessions/{sessionId}/invalidate?reason={reason}", sessionId, reason)
                .retrieve()
                .toBodilessEntity();
    }
}
