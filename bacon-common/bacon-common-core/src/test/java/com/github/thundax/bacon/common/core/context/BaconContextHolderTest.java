package com.github.thundax.bacon.common.core.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BaconContextHolderTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldReturnRequiredContextIds() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        assertThat(BaconContextHolder.requireTenantId()).isEqualTo(1001L);
        assertThat(BaconContextHolder.requireUserId()).isEqualTo(2001L);
    }

    @Test
    void shouldFailWhenRequiredContextIdsAreMissing() {
        assertThatThrownBy(BaconContextHolder::requireTenantId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("tenantId must not be null");
        assertThatThrownBy(BaconContextHolder::requireUserId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("userId must not be null");
    }
}
