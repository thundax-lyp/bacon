package com.github.thundax.bacon.inventory.infra.generator;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TinyIdInventoryReservationNoGenerator implements InventoryReservationNoGenerator {

    private static final String BIZ_TYPE = "inventory_reservation";
    private static final String PREFIX = "RSV-";
    private static final String LOCAL_PREFIX = "RSV-SF-";

    private final LocalSnowflakeIdGenerator localSnowflakeIdGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TinyIdInventoryReservationNoGenerator(LocalSnowflakeIdGenerator localSnowflakeIdGenerator,
                                                 ApplicationEventPublisher applicationEventPublisher) {
        this.localSnowflakeIdGenerator = localSnowflakeIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Retry(name = "inventoryTinyId", fallbackMethod = "nextReservationNoFallback")
    @CircuitBreaker(name = "inventoryTinyId", fallbackMethod = "nextReservationNoFallback")
    public String nextReservationNo() {
        Long id = TinyId.nextId(BIZ_TYPE);
        if (id == null) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_UNAVAILABLE, "tinyid-return-null");
        }
        return PREFIX + id;
    }

    @SuppressWarnings("unused")
    private String nextReservationNoFallback(Throwable throwable) {
        long localId = localSnowflakeIdGenerator.nextId();
        applicationEventPublisher.publishEvent(new InventoryTinyIdFallbackEvent("nextReservationNo",
                throwable.getClass().getSimpleName(), Instant.now()));
        log.error("tinyid-client failed, fallback to local snowflake id, localId={}", localId, throwable);
        return LOCAL_PREFIX + localId;
    }
}
