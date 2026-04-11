package com.github.thundax.bacon.common.mq.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.mq.BaconMqHeaders;
import com.github.thundax.bacon.common.mq.BaconMqMessage;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BaconMqHeaderSupportTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldAppendTenantIdAndUserIdFromContext() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        Map<String, String> headers = BaconMqHeaderSupport.resolveHeaders(BaconMqMessage.forTopic("topic", "key", "p"));

        assertThat(headers)
                .containsEntry(BaconMqHeaders.TENANT_ID, "1001")
                .containsEntry(BaconMqHeaders.USER_ID, "2001");
    }

    @Test
    void shouldKeepExplicitHeaders() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        BaconMqMessage message = BaconMqMessage.forTopic("topic", "key", "p")
                .withHeader(BaconMqHeaders.TENANT_ID, "3001")
                .withHeader(BaconMqHeaders.USER_ID, "4001");

        Map<String, String> headers = BaconMqHeaderSupport.resolveHeaders(message);

        assertThat(headers)
                .containsEntry(BaconMqHeaders.TENANT_ID, "3001")
                .containsEntry(BaconMqHeaders.USER_ID, "4001");
    }
}
