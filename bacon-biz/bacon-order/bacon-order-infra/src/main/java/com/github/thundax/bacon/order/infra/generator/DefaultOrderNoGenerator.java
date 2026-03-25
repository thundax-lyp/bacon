package com.github.thundax.bacon.order.infra.generator;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrderNoGenerator implements OrderNoGenerator {

    private static final String BIZ_TYPE = "order";
    private static final String PREFIX = "ORD-";

    private final IdGenerator idGenerator;

    public DefaultOrderNoGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public String nextOrderNo() {
        long id = idGenerator.nextId(BIZ_TYPE);
        return PREFIX + id;
    }
}
