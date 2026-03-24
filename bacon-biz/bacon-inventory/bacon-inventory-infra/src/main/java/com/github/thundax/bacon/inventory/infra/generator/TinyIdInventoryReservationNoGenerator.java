package com.github.thundax.bacon.inventory.infra.generator;

import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TinyIdInventoryReservationNoGenerator implements InventoryReservationNoGenerator {

    private static final String BIZ_TYPE = "inventory_reservation";

    @Override
    public String nextReservationNo() {
        try {
            Long id = TinyId.nextId(BIZ_TYPE);
            if (id == null) {
                throw new IllegalStateException("TINYID_RETURN_NULL");
            }
            return "RSV-" + id;
        } catch (RuntimeException ex) {
            log.error("tinyid-client failed to generate reservation number", ex);
            throw new IllegalStateException("TINYID_GENERATE_FAILED", ex);
        }
    }
}
