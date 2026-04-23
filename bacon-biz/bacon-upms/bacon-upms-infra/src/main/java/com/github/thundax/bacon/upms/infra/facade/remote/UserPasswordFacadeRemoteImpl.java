package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class UserPasswordFacadeRemoteImpl implements UserPasswordFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public UserPasswordFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.upms-base-url:http://bacon-upms-service/api}") String baseUrl,
            @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public void changePassword(UserPasswordChangeFacadeRequest request) {
        // 改密走 provider 命令端点并携带 body，避免把旧密码/新密码暴露在查询参数或日志里。
        restClient
                .post()
                .uri("/providers/upms/commands/change-current-user-password")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
