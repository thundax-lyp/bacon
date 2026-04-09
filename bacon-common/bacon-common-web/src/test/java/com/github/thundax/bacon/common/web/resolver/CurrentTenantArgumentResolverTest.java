package com.github.thundax.bacon.common.web.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.security.context.CurrentTenantProvider;
import com.github.thundax.bacon.common.web.annotation.CurrentTenant;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

class CurrentTenantArgumentResolverTest {

    @Test
    void shouldSupportAnnotatedLongParameter() throws NoSuchMethodException {
        CurrentTenantArgumentResolver resolver = new CurrentTenantArgumentResolver(() -> 1001L);
        MethodParameter parameter = new MethodParameter(TenantController.class.getMethod("query", Long.class), 0);

        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void shouldResolveTenantFromProvider() throws NoSuchMethodException {
        CurrentTenantProvider provider = () -> 1001L;
        CurrentTenantArgumentResolver resolver = new CurrentTenantArgumentResolver(provider);
        MethodParameter parameter = new MethodParameter(TenantController.class.getMethod("query", Long.class), 0);

        Object tenantId = resolver.resolveArgument(parameter, null, (NativeWebRequest) null, null);
        assertThat(tenantId).isEqualTo(1001L);
    }

    @Test
    void shouldThrowWhenTenantMissing() throws NoSuchMethodException {
        CurrentTenantProvider provider = () -> null;
        CurrentTenantArgumentResolver resolver = new CurrentTenantArgumentResolver(provider);
        MethodParameter parameter = new MethodParameter(TenantController.class.getMethod("query", Long.class), 0);

        assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId is required");
    }

    static class TenantController {

        public void query(@CurrentTenant Long tenantId) {}
    }
}
