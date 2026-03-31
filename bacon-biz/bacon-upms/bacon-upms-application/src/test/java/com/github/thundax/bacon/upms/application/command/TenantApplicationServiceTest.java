package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.enums.UpmsStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantNo;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantApplicationServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private SessionCommandFacade sessionCommandFacade;

    private TenantApplicationService service;

    @BeforeEach
    void setUp() {
        service = new TenantApplicationService(tenantRepository, sessionCommandFacade);
    }

    @Test
    void shouldCreateTenantWithTenantNo() {
        when(tenantRepository.findTenantByTenantNo(new TenantNo("tenant-demo"))).thenReturn(Optional.empty());
        when(tenantRepository.saveTenant(any(Tenant.class)))
                .thenReturn(new Tenant(1L, "tenant-demo", "Demo Tenant", TenantStatus.ENABLED));

        TenantDTO result = service.createTenant("tenant-demo", "Demo Tenant");

        assertThat(result.getTenantNo()).isEqualTo("tenant-demo");
        assertThat(result.getName()).isEqualTo("Demo Tenant");
        assertThat(result.getStatus()).isEqualTo("ENABLED");
    }

    @Test
    void shouldRejectDuplicateTenantNo() {
        when(tenantRepository.findTenantByTenantNo(new TenantNo("tenant-demo")))
                .thenReturn(Optional.of(new Tenant(1L, "tenant-demo", "Demo Tenant", TenantStatus.ENABLED)));

        assertThatThrownBy(() -> service.createTenant("tenant-demo", "Other"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant tenantNo already exists: tenant-demo");
    }

    @Test
    void shouldInvalidateTenantSessionsWhenTenantDisabled() {
        when(tenantRepository.updateTenantStatus(new TenantNo("1001"), "DISABLED"))
                .thenReturn(new Tenant(1L, "1001", "Demo Tenant", TenantStatus.DISABLED));

        TenantDTO result = service.updateTenantStatus("1001", UpmsStatusEnum.DISABLED);

        assertThat(result.getStatus()).isEqualTo("DISABLED");
        verify(sessionCommandFacade).invalidateTenantSessions("1001", "TENANT_DISABLED");
    }
}
