package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.domain.exception.TenantErrorCode;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
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
class TenantCommandApplicationServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SessionCommandFacade sessionCommandFacade;

    @Mock
    private IdGenerator idGenerator;

    private TenantCommandApplicationService service;

    @BeforeEach
    void setUp() {
        service = new TenantCommandApplicationService(tenantRepository, sessionCommandFacade, idGenerator);
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

        TenantDTO result = service.create(new TenantCreateCommand(
                "Demo Tenant", TenantCode.of("TENANT_DEMO"), Instant.parse("2099-01-01T00:00:00Z")));

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

        assertThatThrownBy(() -> service.create(new TenantCreateCommand("Other", TenantCode.of("TENANT_DEMO"), null)))
                .isInstanceOf(UpmsDomainException.class)
                .extracting("code")
                .isEqualTo(TenantErrorCode.TENANT_CODE_ALREADY_EXISTS.code());
    }

    @Test
    void shouldRejectInvalidTenantCode() {
        assertThatThrownBy(
                        () -> service.create(new TenantCreateCommand("Demo Tenant", TenantCode.of("tenant-demo"), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tenantCode must match [A-Z0-9_]+");
    }

    @Test
    void shouldInvalidateTenantSessionsWhenTenantDisabled() {
        when(tenantRepository.findById(TenantId.of(1001L)))
                .thenReturn(Optional.of(tenant(1001L, "Demo Tenant", "TENANT_DEMO", TenantStatus.ACTIVE, null)));
        when(tenantRepository.update(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TenantDTO result = service.updateStatus(
                new TenantStatusUpdateCommand(TenantId.of(1001L), TenantStatus.DISABLED));

        assertThat(result.getStatus()).isEqualTo("DISABLED");
        verify(sessionCommandFacade)
                .invalidateTenantSessions(argThat(request ->
                        request.getTenantId().equals(1001L) && request.getReason().equals("TENANT_DISABLED")));
    }

    private static Tenant tenant(Long id, String name, String code, TenantStatus status, Instant expiredAt) {
        return Tenant.reconstruct(TenantId.of(id), name, TenantCode.of(code), status, expiredAt);
    }
}
