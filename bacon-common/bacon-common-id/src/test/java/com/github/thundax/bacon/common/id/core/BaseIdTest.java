package com.github.thundax.bacon.common.id.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import org.junit.jupiter.api.Test;

class BaseIdTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldKeepTypeSafetyWhenComparingIds() {
        UserId userId = UserId.of(1001L);
        UserId sameUserId = UserId.of(1001L);
        TenantId tenantId = TenantId.of(1001L);

        assertThat(userId).isEqualTo(sameUserId);
        assertThat(userId.hashCode()).isEqualTo(sameUserId.hashCode());
        assertThat(userId).isNotEqualTo(tenantId);
        assertThat(userId.asString()).isEqualTo("1001");
    }

    @Test
    void shouldSerializeAndDeserializeConcreteIdsWithJackson() throws Exception {
        assertThat(objectMapper.writeValueAsString(UserId.of(1001L))).isEqualTo("1001");
        assertThat(objectMapper.readValue("1001", UserId.class)).isEqualTo(UserId.of(1001L));
        assertThat(objectMapper.writeValueAsString(TenantId.of(1L))).isEqualTo("1");
        assertThat(objectMapper.readValue("1", TenantId.class)).isEqualTo(TenantId.of(1L));
        assertThat(objectMapper.writeValueAsString(OperatorId.of("SYSTEM"))).isEqualTo("\"SYSTEM\"");
        assertThat(objectMapper.readValue("\"SYSTEM\"", OperatorId.class)).isEqualTo(OperatorId.of("SYSTEM"));
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> UserId.of((Long) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id value must not be null");
    }

    @Test
    void shouldRejectNonPositiveLongValue() {
        assertThatThrownBy(() -> UserId.of(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must be positive");
    }

    @Test
    void shouldRejectBlankStringValue() {
        assertThatThrownBy(() -> OperatorId.of(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id cannot be blank");
    }
}
