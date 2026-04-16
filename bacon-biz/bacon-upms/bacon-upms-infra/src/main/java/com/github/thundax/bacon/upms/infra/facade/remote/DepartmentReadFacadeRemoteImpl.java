package com.github.thundax.bacon.upms.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.api.request.DepartmentCodeGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.DepartmentGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.DepartmentListFacadeRequest;
import com.github.thundax.bacon.upms.api.response.DepartmentFacadeResponse;
import com.github.thundax.bacon.upms.api.response.DepartmentListFacadeResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class DepartmentReadFacadeRemoteImpl implements DepartmentReadFacade {

    private static final ParameterizedTypeReference<List<DepartmentDTO>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public DepartmentReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.upms-base-url:http://bacon-upms-service/api}") String baseUrl,
            @Value("${bacon.remote.upms.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public DepartmentFacadeResponse getDepartmentById(DepartmentGetFacadeRequest request) {
        // 部门读取固定带 tenantId，调用侧不再暴露旧的租户技术主键。
        DepartmentDTO department = restClient
                .get()
                .uri("/providers/upms/departments/{departmentId}", request.getDepartmentId())
                .retrieve()
                .body(DepartmentDTO.class);
        return DepartmentFacadeResponse.from(department);
    }

    @Override
    public DepartmentFacadeResponse getDepartmentByCode(DepartmentCodeGetFacadeRequest request) {
        DepartmentDTO department = restClient
                .get()
                .uri("/providers/upms/departments/code/{departmentCode}", request.getDepartmentCode())
                .retrieve()
                .body(DepartmentDTO.class);
        return DepartmentFacadeResponse.from(department);
    }

    @Override
    public DepartmentListFacadeResponse listDepartmentsByIds(DepartmentListFacadeRequest request) {
        // 批量部门查询通过重复 queryParam 传主键数组，保持 provider 端可以直接按集合解析。
        List<DepartmentDTO> departments = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/providers/upms/departments")
                        .queryParam("departmentIds", request.getDepartmentIds().toArray())
                        .build())
                .retrieve()
                .body(LIST_TYPE);
        return DepartmentListFacadeResponse.from(departments);
    }
}
