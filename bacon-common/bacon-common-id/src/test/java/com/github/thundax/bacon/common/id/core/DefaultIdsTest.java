package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultIdsTest {

    @Test
    void shouldGenerateTypedIdsWithStableBizTags() {
        RecordingIdGenerator idGenerator = new RecordingIdGenerator();
        DefaultIds ids = new DefaultIds(idGenerator);

        UserId userId = ids.userId();
        UserIdentityId userIdentityId = ids.userIdentityId();
        RoleId roleId = ids.roleId();
        OrderId orderId = ids.orderId();
        SkuId skuId = ids.skuId();

        assertThat(userId).isEqualTo(UserId.of("U1001"));
        assertThat(userIdentityId).isEqualTo(UserIdentityId.of("I1002"));
        assertThat(roleId).isEqualTo(RoleId.of("1003"));
        assertThat(orderId).isEqualTo(OrderId.of(1004L));
        assertThat(skuId).isEqualTo(SkuId.of(1005L));
        assertThat(idGenerator.bizTags).containsExactly("user-id", "user-identity-id", "role-id", "order-id", "sku-id");
    }

    private static final class RecordingIdGenerator implements IdGenerator {

        private final List<String> bizTags = new ArrayList<>();
        private long currentValue = 1000L;

        @Override
        public long nextId(String bizTag) {
            bizTags.add(bizTag);
            currentValue++;
            return currentValue;
        }
    }
}
