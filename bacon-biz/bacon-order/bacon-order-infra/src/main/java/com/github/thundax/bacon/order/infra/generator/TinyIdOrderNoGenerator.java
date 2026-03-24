package com.github.thundax.bacon.order.infra.generator;

import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TinyIdOrderNoGenerator implements OrderNoGenerator {

    private static final String BIZ_TYPE = "order";

    @Override
    public String nextOrderNo() {
        try {
            Long id = TinyId.nextId(BIZ_TYPE);
            if (id == null) {
                throw new IllegalStateException("TINYID_RETURN_NULL");
            }
            return "ORD-" + id;
        } catch (RuntimeException ex) {
            log.error("tinyid-client failed to generate order number", ex);
            throw new IllegalStateException("TINYID_GENERATE_FAILED", ex);
        }
    }
}
