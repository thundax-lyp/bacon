package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.enums.TenantStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import java.time.Instant;
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
    void shouldCreateTenantWithTenantId() {
        when(tenantRepository.findTenantByTenantId(TenantId.of("tenant-demo"))).thenReturn(Optional.empty());
        when(tenantRepository.findTenantByCode("TENANT_DEMO")).thenReturn(Optional.empty());
        when(tenantRepository.saveTenant(any(Tenant.class)))
                .thenReturn(new Tenant("tenant-demo", "Demo Tenant", "TENANT_DEMO", TenantStatus.ACTIVE, Instant.parse("2099-01-01T00:00:00Z")));

        TenantDTO result = service.createTenant("tenant-demo", "Demo Tenant", "TENANT_DEMO",
                Instant.parse("2099-01-01T00:00:00Z"));

        assertThat(result.getId().value()).isEqualTo("tenant-demo");
        assertThat(result.getName()).isEqualTo("Demo Tenant");
        assertThat(result.getTenantCode()).isEqualTo("TENANT_DEMO");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldRejectDuplicateTenantId() {
        when(tenantRepository.findTenantByTenantId(TenantId.of("tenant-demo")))
                .thenReturn(Optional.of(new Tenant("tenant-demo", "Demo Tenant", "TENANT_DEMO",
                        TenantStatus.ACTIVE, Instant.parse("2099-01-01T00:00:00Z"))));

        assertThatThrownBy(() -> service.createTenant("tenant-demo", "Other", "OTHER", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant tenantId already exists: tenant-demo");
    }

    @Test
    void shouldInvalidateTenantSessionsWhenTenantDisabled() {
        when(tenantRepository.updateTenantStatus(TenantId.of("1001"), "DISABLED"))
                .thenReturn(new Tenant("1001", "Demo Tenant", "TENANT_DEMO", TenantStatus.DISABLED, null));

        TenantDTO result = service.updateTenantStatus("1001", TenantStatusEnum.DISABLED);

        assertThat(result.getStatus()).isEqualTo("DISABLED");
        verify(sessionCommandFacade).invalidateTenantSessions("1001", "TENANT_DISABLED");
    }
}
