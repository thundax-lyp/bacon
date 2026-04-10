package com.github.thundax.bacon.common.web.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class BaconContextFilterTest {

    @Test
    void shouldSetAndClearContextAroundRequest() throws Exception {
        BaconContextFilter filter = new BaconContextFilter(request -> new BaconContext(1001L, 2001L));
        FilterChain filterChain = (request, response) -> {
            assertThat(BaconContextHolder.get()).isEqualTo(new BaconContext(1001L, 2001L));
        };

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);
        assertThat(BaconContextHolder.get()).isNull();
    }
}
