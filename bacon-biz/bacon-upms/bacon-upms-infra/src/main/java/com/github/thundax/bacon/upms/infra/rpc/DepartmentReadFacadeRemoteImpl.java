package com.github.thundax.bacon.upms.infra.rpc;

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

    private final RestClient restClient;

    public DepartmentReadFacadeRemoteImpl(@Value("${bacon.remote.upms-base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public DepartmentDTO getDepartmentById(Long tenantId, Long departmentId) {
        return restClient.get()
                .uri("/providers/upms/departments/{departmentId}?tenantId={tenantId}", departmentId, tenantId)
                .retrieve()
                .body(DepartmentDTO.class);
    }

    @Override
    public DepartmentDTO getDepartmentByCode(Long tenantId, String departmentCode) {
        return restClient.get()
                .uri("/providers/upms/departments/code/{departmentCode}?tenantId={tenantId}", departmentCode, tenantId)
                .retrieve()
                .body(DepartmentDTO.class);
    }

    @Override
    public List<DepartmentDTO> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/providers/upms/departments")
                        .queryParam("tenantId", tenantId)
                        .queryParam("departmentIds", departmentIds.toArray())
                        .build())
                .retrieve()
                .body(LIST_TYPE);
    }
}
