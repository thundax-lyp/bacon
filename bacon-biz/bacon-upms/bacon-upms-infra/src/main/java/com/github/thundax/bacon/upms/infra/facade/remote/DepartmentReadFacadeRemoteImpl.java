package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class DepartmentReadFacadeRemoteImpl implements DepartmentReadFacade {

    private static final ParameterizedTypeReference<List<DepartmentDTO>> LIST_TYPE =
            new ParameterizedTypeReference<>() { };
    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public DepartmentReadFacadeRemoteImpl(RestClientFactory restClientFactory,
                                          @Value("${bacon.remote.upms-base-url:http://127.0.0.1:8082/api}") String baseUrl,
                                          @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public DepartmentDTO getDepartmentById(String tenantId, Long departmentId) {
        // 部门读取固定带 tenantId，调用侧不再暴露旧的租户技术主键。
        return restClient.get()
                .uri("/providers/upms/departments/{departmentId}?tenantId={tenantId}", departmentId, tenantId)
                .retrieve()
                .body(DepartmentDTO.class);
    }

    @Override
    public DepartmentDTO getDepartmentByCode(String tenantId, String departmentCode) {
        return restClient.get()
                .uri("/providers/upms/departments/code/{departmentCode}?tenantId={tenantId}", departmentCode, tenantId)
                .retrieve()
                .body(DepartmentDTO.class);
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(String tenantId, Set<Long> departmentIds) {
        // 批量部门查询通过重复 queryParam 传主键数组，保持 provider 端可以直接按集合解析。
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/providers/upms/departments")
                        .queryParam("tenantId", tenantId)
                        .queryParam("departmentIds", departmentIds.toArray())
                        .build())
                .retrieve()
                .body(LIST_TYPE);
    }
}
