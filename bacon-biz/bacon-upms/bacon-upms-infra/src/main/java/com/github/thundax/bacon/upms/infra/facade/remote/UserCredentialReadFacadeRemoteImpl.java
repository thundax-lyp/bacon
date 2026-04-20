package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.facade.UserCredentialReadFacade;
import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserCredentialDetailFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class UserCredentialReadFacadeRemoteImpl implements UserCredentialReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public UserCredentialReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.upms-base-url:http://bacon-upms-service/api}") String baseUrl,
            @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public UserIdentityFacadeResponse getUserIdentity(UserIdentityGetFacadeRequest request) {
        // 身份映射读取只返回绑定结果，不在 remote facade 里补默认身份，避免认证链路误判“用户不存在”和“未绑定”。
        return restClient
                .get()
                .uri(
                        "/providers/upms/user-identities?identityType={identityType}&identityValue={identityValue}",
                        request.getIdentityType(),
                        request.getIdentityValue())
                .retrieve()
                .body(UserIdentityFacadeResponse.class);
    }

    @Override
    public UserCredentialFacadeResponse getUserCredential(UserCredentialGetFacadeRequest request) {
        // 登录凭据查询是 auth 登录链路的基础读操作；provider 负责决定哪些敏感字段可以下发。
        UserCredentialDetailFacadeResponse credential = restClient
                .get()
                .uri(
                        "/providers/upms/user-credentials?identityType={identityType}&identityValue={identityValue}",
                        request.getIdentityType(),
                        request.getIdentityValue())
                .retrieve()
                .body(UserCredentialDetailFacadeResponse.class);
        return UserCredentialFacadeResponse.from(credential);
    }
}
