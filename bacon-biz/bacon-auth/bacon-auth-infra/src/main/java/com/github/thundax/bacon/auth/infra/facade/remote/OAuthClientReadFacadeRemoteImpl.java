package com.github.thundax.bacon.auth.infra.facade.remote;

import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.api.request.OAuthClientGetFacadeRequest;
import com.github.thundax.bacon.auth.api.response.OAuthClientFacadeResponse;
import com.github.thundax.bacon.common.core.config.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OAuthClientReadFacadeRemoteImpl implements OAuthClientReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public OAuthClientReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.auth-base-url:http://bacon-auth-service/api}") String baseUrl,
            @Value("${bacon.remote.auth.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public OAuthClientFacadeResponse getClientByClientId(OAuthClientGetFacadeRequest request) {
        // OAuth client 元数据是授权链路的只读基线，remote facade 只透传 auth 侧定义的客户端配置。
        return restClient
                .get()
                .uri("/providers/auth/queries/oauth-client?clientId={clientId}", request.getClientId())
                .retrieve()
                .body(OAuthClientFacadeResponse.class);
    }
}
