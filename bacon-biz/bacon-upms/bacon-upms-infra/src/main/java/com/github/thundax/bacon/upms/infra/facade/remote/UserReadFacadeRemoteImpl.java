package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import com.github.thundax.bacon.upms.api.request.UserGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserLoginCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityDetailFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserLoginCredentialDetailFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserLoginCredentialFacadeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class UserReadFacadeRemoteImpl implements UserReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public UserReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.upms-base-url:http://bacon-upms-service/api}") String baseUrl,
            @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public UserFacadeResponse getUserById(UserGetFacadeRequest request) {
        // 用户主数据读取按 tenantId + userId 定位，避免在调用侧绕过租户边界。
        UserDTO user = restClient
                .get()
                .uri("/providers/upms/users/{userId}", request.getUserId())
                .retrieve()
                .body(UserDTO.class);
        return UserFacadeResponse.from(user);
    }

    @Override
    public UserIdentityFacadeResponse getUserIdentity(UserIdentityGetFacadeRequest request) {
        // 身份映射读取只返回绑定结果，不在 remote facade 里补默认身份，避免认证链路误判“用户不存在”和“未绑定”。
        UserIdentityDetailFacadeResponse userIdentity = restClient
                .get()
                .uri(
                        "/providers/upms/user-identities?identityType={identityType}&identityValue={identityValue}",
                        request.getIdentityType(),
                        request.getIdentityValue())
                .retrieve()
                .body(UserIdentityDetailFacadeResponse.class);
        return UserIdentityFacadeResponse.from(userIdentity);
    }

    @Override
    public UserLoginCredentialFacadeResponse getUserLoginCredential(UserLoginCredentialGetFacadeRequest request) {
        // 登录凭据查询是 auth 登录链路的基础读操作；provider 负责决定哪些敏感字段可以下发。
        UserLoginCredentialDetailFacadeResponse credential = restClient
                .get()
                .uri(
                        "/providers/upms/user-credentials?identityType={identityType}&identityValue={identityValue}",
                        request.getIdentityType(),
                        request.getIdentityValue())
                .retrieve()
                .body(UserLoginCredentialDetailFacadeResponse.class);
        return UserLoginCredentialFacadeResponse.from(credential);
    }

    @Override
    public TenantFacadeResponse getTenantByTenantId() {
        // tenant 查询固定按 tenantId 读取，不再暴露重复的租户编码语义。
        TenantDTO tenant = restClient
                .get()
                .uri("/providers/upms/tenants/current")
                .retrieve()
                .body(TenantDTO.class);
        return TenantFacadeResponse.from(tenant);
    }
}
