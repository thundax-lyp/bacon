package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class UserReadFacadeRemoteImpl implements UserReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public UserReadFacadeRemoteImpl(RestClientFactory restClientFactory,
                                    @Value("${bacon.remote.upms-base-url:http://127.0.0.1:8082/api}") String baseUrl,
                                    @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public UserDTO getUserById(Long tenantId, Long userId) {
        // 用户主数据读取按 tenantId + userId 定位，避免在调用侧绕过租户边界。
        return restClient.get()
                .uri("/providers/upms/users/{userId}?tenantId={tenantId}", userId, tenantId)
                .retrieve()
                .body(UserDTO.class);
    }

    @Override
    public UserIdentityDTO getUserIdentity(Long tenantId, String identityType, String identityValue) {
        // 身份映射读取只返回绑定结果，不在 remote facade 里补默认身份，避免认证链路误判“用户不存在”和“未绑定”。
        return restClient.get()
                .uri("/providers/upms/user-identities?tenantId={tenantId}&identityType={identityType}&identityValue={identityValue}",
                        tenantId, identityType, identityValue)
                .retrieve()
                .body(UserIdentityDTO.class);
    }

    @Override
    public UserLoginCredentialDTO getUserLoginCredential(Long tenantId, String identityType, String identityValue) {
        // 登录凭据查询是 auth 登录链路的基础读操作；provider 负责决定哪些敏感字段可以下发。
        return restClient.get()
                .uri("/providers/upms/user-credentials?tenantId={tenantId}&identityType={identityType}&identityValue={identityValue}",
                        tenantId, identityType, identityValue)
                .retrieve()
                .body(UserLoginCredentialDTO.class);
    }

    @Override
    public TenantDTO getTenantByTenantId(Long tenantId) {
        // tenant 查询固定按 tenantId 读取，不再暴露重复的租户编码语义。
        return restClient.get()
                .uri("/providers/upms/tenants/{tenantId}", tenantId)
                .retrieve()
                .body(TenantDTO.class);
    }
}
