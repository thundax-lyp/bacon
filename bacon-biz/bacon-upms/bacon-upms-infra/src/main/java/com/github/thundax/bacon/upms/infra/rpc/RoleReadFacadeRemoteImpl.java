package com.github.thundax.bacon.upms.infra.rpc;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class RoleReadFacadeRemoteImpl implements RoleReadFacade {

    private static final ParameterizedTypeReference<List<RoleDTO>> LIST_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public RoleReadFacadeRemoteImpl(@Value("${bacon.remote.upms-base-url:http://127.0.0.1:8082/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public RoleDTO getRoleById(Long tenantId, Long roleId) {
        return restClient.get()
                .uri("/providers/upms/roles/{roleId}?tenantId={tenantId}", roleId, tenantId)
                .retrieve()
                .body(RoleDTO.class);
    }

    @Override
    public List<RoleDTO> getRolesByUserId(Long tenantId, Long userId) {
        return restClient.get()
                .uri("/providers/upms/roles?tenantId={tenantId}&userId={userId}", tenantId, userId)
                .retrieve()
                .body(LIST_TYPE);
    }
}
