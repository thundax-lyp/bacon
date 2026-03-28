package com.github.thundax.bacon.common.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InternalApiGuardInterceptorTest {

    @Test
    void shouldAllowProviderRequestWhenTokenMatches() throws Exception {
        InternalApiGuardProperties properties = properties();
        InternalApiGuardInterceptor interceptor = new InternalApiGuardInterceptor(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/providers/storage/objects/100");
        request.addHeader("X-Bacon-Provider-Token", "storage-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldRejectProviderRequestWhenTokenMissing() throws Exception {
        InternalApiGuardProperties properties = properties();
        InternalApiGuardInterceptor interceptor = new InternalApiGuardInterceptor(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/providers/storage/objects/100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void shouldRejectProviderRequestWhenTokenInvalid() throws Exception {
        InternalApiGuardProperties properties = properties();
        InternalApiGuardInterceptor interceptor = new InternalApiGuardInterceptor(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/providers/storage/objects/100");
        request.addHeader("X-Bacon-Provider-Token", "wrong-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    private InternalApiGuardProperties properties() {
        InternalApiGuardProperties properties = new InternalApiGuardProperties();
        properties.setEnabled(true);
        properties.setToken("storage-token");
        properties.setIncludePathPatterns(List.of("/providers/storage/**"));
        return properties;
    }
}
