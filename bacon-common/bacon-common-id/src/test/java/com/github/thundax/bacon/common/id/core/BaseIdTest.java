package com.github.thundax.bacon.common.id.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseIdTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldKeepTypeSafetyWhenComparingIds() {
        UserId userId = UserId.of("U1001");
        UserId sameUserId = UserId.of("U1001");
        RoleId roleId = RoleId.of("1001");

        assertThat(userId).isEqualTo(sameUserId);
        assertThat(userId.hashCode()).isEqualTo(sameUserId.hashCode());
        assertThat(userId).isNotEqualTo(roleId);
        assertThat(userId.asString()).isEqualTo("U1001");
    }

    @Test
    void shouldSerializeAndDeserializeConcreteIdsWithJackson() throws Exception {
        assertThat(objectMapper.writeValueAsString(UserId.of("U1001"))).isEqualTo("\"U1001\"");
        assertThat(objectMapper.readValue("\"U1001\"", UserId.class)).isEqualTo(UserId.of("U1001"));
        assertThat(objectMapper.writeValueAsString(TenantId.of("T001"))).isEqualTo("\"T001\"");
        assertThat(objectMapper.readValue("\"T001\"", TenantId.class)).isEqualTo(TenantId.of("T001"));
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> UserId.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id value must not be null");
    }

    @Test
    void shouldRejectBlankStringValue() {
        assertThatThrownBy(() -> UserId.of(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id cannot be blank");
    }

    @Test
    void shouldRejectNonPositiveLongValue() {
        assertThatThrownBy(() -> SkuId.of(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must be positive");
    }
}
