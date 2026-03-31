package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
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
    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public RoleReadFacadeRemoteImpl(RestClientFactory restClientFactory,
                                    @Value("${bacon.remote.upms-base-url:http://127.0.0.1:8082/api}") String baseUrl,
                                    @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public RoleDTO getRoleById(String tenantNo, Long roleId) {
        // 单角色读取保持最小读取模型，不把数据权限等衍生信息混进主数据 DTO。
        return restClient.get()
                .uri("/providers/upms/roles/{roleId}?tenantNo={tenantNo}", roleId, tenantNo)
                .retrieve()
                .body(RoleDTO.class);
    }

    @Override
    public List<RoleDTO> getRolesByUserId(String tenantNo, String userId) {
        // 用户角色列表直接以 upms 聚合结果为准，调用方不再本地拼接用户-角色关系。
        return restClient.get()
                .uri("/providers/upms/roles?tenantNo={tenantNo}&userId={userId}", tenantNo, userId)
                .retrieve()
                .body(LIST_TYPE);
    }
}
