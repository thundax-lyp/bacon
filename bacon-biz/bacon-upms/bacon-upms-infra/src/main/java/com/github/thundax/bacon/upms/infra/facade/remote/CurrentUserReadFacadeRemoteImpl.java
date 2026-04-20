package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.upms.api.facade.CurrentUserReadFacade;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class CurrentUserReadFacadeRemoteImpl implements CurrentUserReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public CurrentUserReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.upms-base-url:http://bacon-upms-service/api}") String baseUrl,
            @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public UserFacadeResponse getCurrentUser() {
        // 当前用户主数据读取固定由 upms 按上下文用户解析，不让调用侧传 userId。
        return restClient
                .get()
                .uri("/providers/upms/users/current")
                .retrieve()
                .body(UserFacadeResponse.class);
    }

    @Override
    public TenantFacadeResponse getCurrentTenant() {
        // 当前租户读取复用稳定的按 tenantId provider 契约，避免维护额外的 current tenant 入口。
        return restClient
                .get()
                .uri("/providers/upms/tenants/{tenantId}", BaconIdContextHelper.requireTenantId())
                .retrieve()
                .body(TenantFacadeResponse.class);
    }

    @Override
    public UserDataScopeFacadeResponse getCurrentDataScope() {
        // 当前用户数据权限固定由 upms 按上下文用户解析，不在调用侧透传 userId。
        return restClient
                .get()
                .uri("/providers/upms/permissions/current/data-scope")
                .retrieve()
                .body(UserDataScopeFacadeResponse.class);
    }
}
