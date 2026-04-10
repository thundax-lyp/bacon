package com.github.thundax.bacon.common.web.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class DefaultBaconContextResolverTest {

    private final DefaultBaconContextResolver resolver = new DefaultBaconContextResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveContextFromHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Tenant-Id", "1001");
        request.addHeader("X-User-Id", "2001");

        BaconContext context = resolver.resolve(request);

        assertThat(context).isEqualTo(new BaconContext(1001L, 2001L));
    }

    @Test
    void shouldResolveContextFromAuthentication() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(Map.of("tenantId", 1001L, "userId", 2001L), null, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        BaconContext context = resolver.resolve(new MockHttpServletRequest());

        assertThat(context).isEqualTo(new BaconContext(1001L, 2001L));
    }
}
