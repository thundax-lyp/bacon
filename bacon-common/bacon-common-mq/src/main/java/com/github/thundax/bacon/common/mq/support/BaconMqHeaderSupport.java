package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.mq.BaconMqHeaders;
import com.github.thundax.bacon.common.mq.BaconMqMessage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BaconMqHeaderSupport {

    private BaconMqHeaderSupport() {}

    public static Map<String, String> resolveHeaders(BaconMqMessage message) {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>(message.getHeaders());
        Long tenantId = BaconContextHolder.currentTenantId();
        if (tenantId != null) {
            headers.putIfAbsent(BaconMqHeaders.TENANT_ID, String.valueOf(tenantId));
        }
        return headers;
    }
}
