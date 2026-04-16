package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.api.request.RoleGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.RoleListByUserFacadeRequest;
import com.github.thundax.bacon.upms.api.response.RoleFacadeResponse;
import com.github.thundax.bacon.upms.api.response.RoleListFacadeResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class RoleReadFacadeRemoteImpl implements RoleReadFacade {

    private static final ParameterizedTypeReference<List<RoleDTO>> LIST_TYPE = new ParameterizedTypeReference<>() {};
    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public RoleReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.upms-base-url:http://bacon-upms-service/api}") String baseUrl,
            @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public RoleFacadeResponse getRoleById(RoleGetFacadeRequest request) {
        // 单角色读取保持最小读取模型，不把数据权限等衍生信息混进主数据 DTO。
        RoleDTO role = restClient
                .get()
                .uri("/providers/upms/roles/{roleId}", request.getRoleId())
                .retrieve()
                .body(RoleDTO.class);
        return RoleFacadeResponse.from(role);
    }

    @Override
    public RoleListFacadeResponse getRolesByUserId(RoleListByUserFacadeRequest request) {
        // 用户角色列表直接以 upms 聚合结果为准，调用方不再本地拼接用户-角色关系。
        List<RoleDTO> roles = restClient
                .get()
                .uri("/providers/upms/roles?userId={userId}", request.getUserId())
                .retrieve()
                .body(LIST_TYPE);
        return RoleListFacadeResponse.from(roles);
    }
}
