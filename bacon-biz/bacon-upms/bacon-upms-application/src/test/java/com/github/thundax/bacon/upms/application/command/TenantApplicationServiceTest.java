package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantApplicationServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SessionCommandFacade sessionCommandFacade;

    @Mock
    private IdGenerator idGenerator;

    private TenantApplicationService service;

    @BeforeEach
    void setUp() {
        service = new TenantApplicationService(tenantRepository, sessionCommandFacade, idGenerator);
    }

    @Test
    void shouldCreateTenantWithTenantId() {
        when(idGenerator.nextId("tenant-id")).thenReturn(1001L);
        when(tenantRepository.findByCode(TenantCode.of("TENANT_DEMO"))).thenReturn(Optional.empty());
        when(tenantRepository.insert(any(Tenant.class)))
                .thenReturn(tenant(
                        1001L,
                        "Demo Tenant",
                        "TENANT_DEMO",
                        TenantStatus.ACTIVE,
                        Instant.parse("2099-01-01T00:00:00Z")));

        TenantDTO result = service.createTenant(
                "Demo Tenant", TenantCode.of("TENANT_DEMO"), Instant.parse("2099-01-01T00:00:00Z"));

        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getName()).isEqualTo("Demo Tenant");
        assertThat(result.getCode()).isEqualTo("TENANT_DEMO");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldRejectDuplicateTenantCode() {
        when(idGenerator.nextId("tenant-id")).thenReturn(1001L);
        when(tenantRepository.findByCode(TenantCode.of("TENANT_DEMO")))
                .thenReturn(Optional.of(tenant(
                        1001L,
                        "Demo Tenant",
                        "TENANT_DEMO",
                        TenantStatus.ACTIVE,
                        Instant.parse("2099-01-01T00:00:00Z"))));

        assertThatThrownBy(() -> service.createTenant("Other", TenantCode.of("TENANT_DEMO"), null))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Tenant code already exists: TENANT_DEMO");
    }

    @Test
    void shouldRejectInvalidTenantCode() {
        assertThatThrownBy(() -> service.createTenant("Demo Tenant", TenantCode.of("tenant-demo"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tenantCode must match [A-Z0-9_]+");
    }

    @Test
    void shouldInvalidateTenantSessionsWhenTenantDisabled() {
        when(tenantRepository.findById(TenantId.of(1001L)))
                .thenReturn(Optional.of(tenant(1001L, "Demo Tenant", "TENANT_DEMO", TenantStatus.ACTIVE, null)));
        when(tenantRepository.update(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TenantDTO result = service.updateTenantStatus(TenantId.of(1001L), TenantStatus.DISABLED);

        assertThat(result.getStatus()).isEqualTo("DISABLED");
        verify(sessionCommandFacade)
                .invalidateTenantSessions(new SessionInvalidateTenantFacadeRequest(1001L, "TENANT_DISABLED"));
    }

    private static Tenant tenant(Long id, String name, String code, TenantStatus status, Instant expiredAt) {
        return Tenant.create(TenantId.of(id), name, TenantCode.of(code), status, expiredAt);
    }
}
