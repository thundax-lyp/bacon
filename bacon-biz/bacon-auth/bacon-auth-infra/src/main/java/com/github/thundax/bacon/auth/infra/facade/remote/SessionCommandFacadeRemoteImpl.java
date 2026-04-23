package com.github.thundax.bacon.auth.infra.facade.remote;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.common.core.config.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class SessionCommandFacadeRemoteImpl implements SessionCommandFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public SessionCommandFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.auth-base-url:http://bacon-auth-service/api}") String baseUrl,
            @Value("${bacon.remote.auth.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public void invalidateUserSessions(SessionInvalidateUserFacadeRequest request) {
        // 会话失效走 provider 命令端点，不吞异常；调用方据此决定是回滚主流程还是继续补偿。
        restClient
                .post()
                .uri(
                        "/providers/auth/commands/invalidate-user-sessions?tenantId={tenantId}&userId={userId}&reason={reason}",
                        request.getTenantId(),
                        request.getUserId(),
                        request.getReason())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void invalidateTenantSessions(SessionInvalidateTenantFacadeRequest request) {
        // 租户级失效属于批量安全操作，remote facade 只转发命令，不在客户端侧做分批或降级。
        restClient
                .post()
                .uri(
                        "/providers/auth/commands/invalidate-tenant-sessions?tenantId={tenantId}&reason={reason}",
                        request.getTenantId(),
                        request.getReason())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void invalidateSession(SessionInvalidateFacadeRequest request) {
        // 单会话失效用于登出或风控封禁，失败时直接把远端异常暴露给上游。
        restClient
                .post()
                .uri(
                        "/providers/auth/commands/invalidate-session?sessionId={sessionId}&reason={reason}",
                        request.getSessionId(),
                        request.getReason())
                .retrieve()
                .toBodilessEntity();
    }
}
