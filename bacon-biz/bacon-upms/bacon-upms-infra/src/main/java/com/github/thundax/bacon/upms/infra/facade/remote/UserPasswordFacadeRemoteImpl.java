package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.UserPasswordChangeDTO;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class UserPasswordFacadeRemoteImpl implements UserPasswordFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public UserPasswordFacadeRemoteImpl(RestClientFactory restClientFactory,
                                        @Value("${bacon.remote.upms-base-url:http://127.0.0.1:8082/api}") String baseUrl,
                                        @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public void changePassword(@NonNull TenantId tenantId, @NonNull UserId userId, String oldPassword, String newPassword) {
        // 改密走 provider 命令端点并携带 body，避免把旧密码/新密码暴露在查询参数或日志里。
        restClient.post()
                .uri("/providers/upms/users/{userId}/password/change?tenantId={tenantId}", userId.value(), tenantId.value())
                .body(new UserPasswordChangeDTO(oldPassword, newPassword))
                .retrieve()
                .toBodilessEntity();
    }
}
