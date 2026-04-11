package com.github.thundax.bacon.common.id.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BaconIdContextHelperTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldReturnTypedIdsFromContext() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        assertThat(BaconIdContextHelper.currentTenantId()).isEqualTo(TenantId.of(1001L));
        assertThat(BaconIdContextHelper.currentUserId()).isEqualTo(UserId.of(2001L));
    }

    @Test
    void shouldReturnNullWhenContextIsMissing() {
        assertThat(BaconIdContextHelper.currentTenantId()).isNull();
        assertThat(BaconIdContextHelper.currentUserId()).isNull();
    }

    @Test
    void shouldRequireTypedIdsFromContext() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        assertThat(BaconIdContextHelper.requireTenantId()).isEqualTo(TenantId.of(1001L));
        assertThat(BaconIdContextHelper.requireUserId()).isEqualTo(UserId.of(2001L));
    }

    @Test
    void shouldFailWhenRequiredContextIdsAreMissing() {
        assertThatThrownBy(BaconIdContextHelper::requireTenantId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("tenantId must not be null");
        assertThatThrownBy(BaconIdContextHelper::requireUserId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("userId must not be null");
    }
}
