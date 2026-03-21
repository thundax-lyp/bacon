package com.github.thundax.bacon.upms.infra.rpc;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class PermissionReadFacadeRemoteImpl implements PermissionReadFacade {

    private static final ParameterizedTypeReference<List<UserMenuTreeDTO>> MENU_LIST_TYPE =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<Set<String>> CODE_SET_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public PermissionReadFacadeRemoteImpl(@Value("${bacon.remote.upms-base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public List<UserMenuTreeDTO> getUserMenuTree(Long tenantId, Long userId) {
        return restClient.get()
                .uri("/providers/upms/permissions/menus?tenantId={tenantId}&userId={userId}", tenantId, userId)
                .retrieve()
                .body(MENU_LIST_TYPE);
    }

    @Override
    public Set<String> getUserPermissionCodes(Long tenantId, Long userId) {
        return restClient.get()
                .uri("/providers/upms/permissions/codes?tenantId={tenantId}&userId={userId}", tenantId, userId)
                .retrieve()
                .body(CODE_SET_TYPE);
    }

    @Override
    public UserDataScopeDTO getUserDataScope(Long tenantId, Long userId) {
        return restClient.get()
                .uri("/providers/upms/permissions/data-scope?tenantId={tenantId}&userId={userId}", tenantId, userId)
                .retrieve()
                .body(UserDataScopeDTO.class);
    }
}
