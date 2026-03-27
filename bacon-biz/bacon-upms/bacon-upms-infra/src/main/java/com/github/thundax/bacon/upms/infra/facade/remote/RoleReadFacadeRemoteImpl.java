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

    private final RestClient restClient;

    public RoleReadFacadeRemoteImpl(RestClientFactory restClientFactory,
                                    @Value("${bacon.remote.upms-base-url:http://127.0.0.1:8082/api}") String baseUrl) {
        this.restClient = restClientFactory.create(baseUrl);
    }

    @Override
    public RoleDTO getRoleById(Long tenantId, Long roleId) {
        // 单角色读取保持最小读取模型，不把数据权限等衍生信息混进主数据 DTO。
        return restClient.get()
                .uri("/providers/upms/roles/{roleId}?tenantId={tenantId}", roleId, tenantId)
                .retrieve()
                .body(RoleDTO.class);
    }

    @Override
    public List<RoleDTO> getRolesByUserId(Long tenantId, Long userId) {
        // 用户角色列表直接以 upms 聚合结果为准，调用方不再本地拼接用户-角色关系。
        return restClient.get()
                .uri("/providers/upms/roles?tenantId={tenantId}&userId={userId}", tenantId, userId)
                .retrieve()
                .body(LIST_TYPE);
    }
}
