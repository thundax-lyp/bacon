package com.github.thundax.bacon.inventory.infra.generator;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.event.IdFallbackEvent;
import com.github.thundax.bacon.common.id.provider.SnowflakeIdGenerator;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderInventoryReservationNoGenerator implements InventoryReservationNoGenerator {

    private static final String BIZ_TYPE = "inventory_reservation";
    private static final String PREFIX = "RSV-";
    private static final String LOCAL_PREFIX = "RSV-SF-";

    private final IdGenerator idGenerator;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ProviderInventoryReservationNoGenerator(IdGenerator idGenerator,
                                                   SnowflakeIdGenerator snowflakeIdGenerator,
                                                   ApplicationEventPublisher applicationEventPublisher) {
        this.idGenerator = idGenerator;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Retry(name = "inventoryTinyId", fallbackMethod = "nextReservationNoFallback")
    @CircuitBreaker(name = "inventoryTinyId", fallbackMethod = "nextReservationNoFallback")
    public String nextReservationNo() {
        long id = idGenerator.nextId(BIZ_TYPE);
        if (id <= 0L) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_UNAVAILABLE,
                    "id-provider-return-invalid");
        }
        return PREFIX + id;
    }

    @SuppressWarnings("unused")
    private String nextReservationNoFallback(Throwable throwable) {
        long localId = snowflakeIdGenerator.nextId();
        applicationEventPublisher.publishEvent(new IdFallbackEvent(BIZ_TYPE, "nextReservationNo",
                throwable.getClass().getSimpleName(), Instant.now()));
        log.error("id provider failed, fallback to local snowflake id, localId={}", localId, throwable);
        return LOCAL_PREFIX + localId;
    }
}
