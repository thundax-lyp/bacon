package com.github.thundax.bacon.inventory.infra.generator;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

@Component
public class ProviderInventoryReservationNoGenerator implements InventoryReservationNoGenerator {

    private static final String BIZ_TYPE = "inventory_reservation";
    private static final String PREFIX = "RSV-";

    private final IdGenerator idGenerator;

    public ProviderInventoryReservationNoGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    @Retry(name = "inventoryTinyId")
    @CircuitBreaker(name = "inventoryTinyId")
    public String nextReservationNo() {
        try {
            long id = idGenerator.nextId(BIZ_TYPE);
            if (id <= 0L) {
                throw new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_REMOTE_UNAVAILABLE, "id-provider-return-invalid");
            }
            return PREFIX + id;
        } catch (InventoryDomainException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new InventoryDomainException(
                    InventoryErrorCode.INVENTORY_REMOTE_UNAVAILABLE, "id-provider-call-failed", ex);
        }
    }
}
