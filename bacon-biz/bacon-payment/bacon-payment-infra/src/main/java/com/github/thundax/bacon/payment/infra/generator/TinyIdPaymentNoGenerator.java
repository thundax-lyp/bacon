package com.github.thundax.bacon.payment.infra.generator;

import com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TinyIdPaymentNoGenerator implements PaymentNoGenerator {

    private static final String BIZ_TYPE = "payment";

    @Override
    public String nextPaymentNo() {
        try {
            Long id = TinyId.nextId(BIZ_TYPE);
            if (id == null) {
                throw new IllegalStateException("TINYID_RETURN_NULL");
            }
            return "PAY-" + id;
        } catch (RuntimeException ex) {
            log.error("tinyid-client failed to generate payment number", ex);
            throw new IllegalStateException("TINYID_GENERATE_FAILED", ex);
        }
    }
}
