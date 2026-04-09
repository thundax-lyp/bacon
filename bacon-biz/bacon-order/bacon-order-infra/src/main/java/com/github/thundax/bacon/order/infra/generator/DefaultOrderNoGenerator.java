package com.github.thundax.bacon.order.infra.generator;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrderNoGenerator implements OrderNoGenerator {

    private static final String BIZ_TYPE = "order";
    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final IdGenerator idGenerator;

    public DefaultOrderNoGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public OrderNo nextOrderNo() {
        long id = idGenerator.nextId(BIZ_TYPE);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String suffix = String.format("%06d", Math.floorMod(id, 1_000_000L));
        return OrderNo.of(PREFIX + timestamp + suffix);
    }
}
