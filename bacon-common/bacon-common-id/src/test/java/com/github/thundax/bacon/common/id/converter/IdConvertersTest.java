package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdConvertersTest {

    @Test
    void shouldConvertBetweenValueAndIdentifier() {
        Long nullableUserIdValue = IdConverters.toValue((UserId) null);
        UserId nullableUserId = IdConverters.fromValue((Long) null, UserId::of);

        assertThat(IdConverters.toValue(UserId.of(1001L))).isEqualTo(1001L);
        assertThat(IdConverters.toValue(TenantId.of("T001"))).isEqualTo("T001");
        assertThat(nullableUserIdValue).isNull();
        assertThat(IdConverters.fromValue(1001L, UserId::of)).isEqualTo(UserId.of(1001L));
        assertThat(IdConverters.fromValue("T001", TenantId::of)).isEqualTo(TenantId.of("T001"));
        assertThat(nullableUserId).isNull();
    }
}
