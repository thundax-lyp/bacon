package com.github.thundax.bacon.auth.infra.facade.remote;

import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;
import com.github.thundax.bacon.common.core.config.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class TokenVerifyFacadeRemoteImpl implements TokenVerifyFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public TokenVerifyFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.auth-base-url:http://bacon-auth-service/api}") String baseUrl,
            @Value("${bacon.remote.auth.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public SessionValidationFacadeResponse verifyAccessToken(TokenVerifyFacadeRequest request) {
        // 鉴权链路依赖这里返回标准化的 session 校验结果；remote facade 不做本地缓存，始终以 auth 为准。
        return restClient
                .get()
                .uri("/providers/auth/queries/verify-token?accessToken={accessToken}", request.getAccessToken())
                .retrieve()
                .body(SessionValidationFacadeResponse.class);
    }

    @Override
    public CurrentSessionFacadeResponse getSessionContext(SessionContextGetFacadeRequest request) {
        // session 上下文查询和 token 校验拆成两个端点，避免每次鉴权都返回超出当前场景的会话细节。
        return restClient
                .get()
                .uri("/providers/auth/queries/session-context?sessionId={sessionId}", request.getSessionId())
                .retrieve()
                .body(CurrentSessionFacadeResponse.class);
    }
}
