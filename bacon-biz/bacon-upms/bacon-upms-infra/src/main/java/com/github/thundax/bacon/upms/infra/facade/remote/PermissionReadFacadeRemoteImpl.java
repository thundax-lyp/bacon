package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class PermissionReadFacadeRemoteImpl implements PermissionReadFacade {

    private static final ParameterizedTypeReference<List<UserMenuTreeDTO>> MENU_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Set<String>> CODE_SET_TYPE = new ParameterizedTypeReference<>() {};
    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public PermissionReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.upms-base-url:http://bacon-upms-service/api}") String baseUrl,
            @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public List<UserMenuTreeDTO> getUserMenuTree(@NonNull UserId userId) {
        // 菜单树属于已聚合好的读取模型，客户端只透传，不在本地再次做权限裁剪。
        return restClient
                .get()
                .uri("/providers/upms/permissions/menus?userId={userId}", userId.value())
                .retrieve()
                .body(MENU_LIST_TYPE);
    }

    @Override
    public Set<String> getUserPermissionCodes(@NonNull UserId userId) {
        // 权限码集合用于鉴权快速判断，保持去重后的集合返回，避免调用方再做一次归并。
        return restClient
                .get()
                .uri("/providers/upms/permissions/codes?userId={userId}", userId.value())
                .retrieve()
                .body(CODE_SET_TYPE);
    }

    @Override
    public UserDataScopeDTO getUserDataScope(@NonNull UserId userId) {
        // 数据权限规则统一由 upms 侧计算，remote facade 不在消费者侧复制同样的合并逻辑。
        return restClient
                .get()
                .uri("/providers/upms/permissions/data-scope?userId={userId}", userId.value())
                .retrieve()
                .body(UserDataScopeDTO.class);
    }
}
