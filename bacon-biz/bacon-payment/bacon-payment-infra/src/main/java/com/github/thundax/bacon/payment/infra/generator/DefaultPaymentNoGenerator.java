package com.github.thundax.bacon.payment.infra.generator;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator;
import org.springframework.stereotype.Component;

@Component
public class DefaultPaymentNoGenerator implements PaymentNoGenerator {

    private static final String BIZ_TYPE = "payment";
    private static final String PREFIX = "PAY-";

    private final IdGenerator idGenerator;

    public DefaultPaymentNoGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public String nextPaymentNo() {
        long id = idGenerator.nextId(BIZ_TYPE);
        return PREFIX + id;
    }
}
