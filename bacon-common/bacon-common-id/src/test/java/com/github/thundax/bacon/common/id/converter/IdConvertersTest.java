package com.github.thundax.bacon.common.id.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import org.junit.jupiter.api.Test;

class IdConvertersTest {

    @Test
    void shouldConvertBetweenValueAndIdentifier() {
        Long nullableUserIdValue = IdConverters.toValue((UserId) null);
        UserId nullableUserId = IdConverters.fromValue((Long) null, value -> UserId.of(value));
        UserId userId = IdConverters.fromValue(1001L, value -> UserId.of(value));
        TenantId tenantId = IdConverters.fromValue(1L, value -> TenantId.of(value));

        assertThat(IdConverters.toValue(UserId.of(1001L))).isEqualTo(1001L);
        assertThat(IdConverters.toValue(TenantId.of(1L))).isEqualTo(1L);
        assertThat(nullableUserIdValue).isNull();
        assertThat(userId).isEqualTo(UserId.of(1001L));
        assertThat(tenantId).isEqualTo(TenantId.of(1L));
        assertThat(nullableUserId).isNull();
    }
}
