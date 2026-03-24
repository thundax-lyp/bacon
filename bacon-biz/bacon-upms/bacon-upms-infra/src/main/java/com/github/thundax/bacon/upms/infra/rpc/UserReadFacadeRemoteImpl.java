package com.github.thundax.bacon.upms.infra.rpc;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class UserReadFacadeRemoteImpl implements UserReadFacade {

    private final RestClient restClient;

    public UserReadFacadeRemoteImpl(@Value("${bacon.remote.upms-base-url:http://127.0.0.1:8082/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public UserDTO getUserById(Long tenantId, Long userId) {
        return restClient.get()
                .uri("/providers/upms/users/{userId}?tenantId={tenantId}", userId, tenantId)
                .retrieve()
                .body(UserDTO.class);
    }

    @Override
    public UserIdentityDTO getUserIdentity(Long tenantId, String identityType, String identityValue) {
        return restClient.get()
                .uri("/providers/upms/user-identities?tenantId={tenantId}&identityType={identityType}&identityValue={identityValue}",
                        tenantId, identityType, identityValue)
                .retrieve()
                .body(UserIdentityDTO.class);
    }

    @Override
    public UserLoginCredentialDTO getUserLoginCredential(Long tenantId, String identityType, String identityValue) {
        return restClient.get()
                .uri("/providers/upms/user-credentials?tenantId={tenantId}&identityType={identityType}&identityValue={identityValue}",
                        tenantId, identityType, identityValue)
                .retrieve()
                .body(UserLoginCredentialDTO.class);
    }

    @Override
    public TenantDTO getTenantByTenantId(Long tenantId) {
        return restClient.get()
                .uri("/providers/upms/tenants/{tenantId}", tenantId)
                .retrieve()
                .body(TenantDTO.class);
    }
}
