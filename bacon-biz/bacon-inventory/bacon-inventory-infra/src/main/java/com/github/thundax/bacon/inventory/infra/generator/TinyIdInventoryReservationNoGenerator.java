package com.github.thundax.bacon.inventory.infra.generator;

import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TinyIdInventoryReservationNoGenerator implements InventoryReservationNoGenerator {

    private static final String TINY_ID_CLASS = "com.xiaoju.uemc.tinyid.client.utils.TinyId";
    private static final String BIZ_TYPE = "inventory_reservation";

    @Override
    public String nextReservationNo() {
        try {
            Class<?> tinyIdClass = Class.forName(TINY_ID_CLASS);
            Method nextIdMethod = tinyIdClass.getMethod("nextId", String.class);
            Object id = nextIdMethod.invoke(null, BIZ_TYPE);
            if (!(id instanceof Number number)) {
                throw new IllegalStateException("TINYID_RETURN_TYPE_INVALID");
            }
            return "RSV-" + number.longValue();
        } catch (ClassNotFoundException ex) {
            log.error("tinyid-client is not available on the classpath");
            throw new IllegalStateException("TINYID_CLIENT_UNAVAILABLE", ex);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            log.error("tinyid-client API is incompatible with current integration");
            throw new IllegalStateException("TINYID_CLIENT_API_INCOMPATIBLE", ex);
        } catch (InvocationTargetException ex) {
            log.error("tinyid-client failed to generate reservation number", ex.getTargetException());
            throw new IllegalStateException("TINYID_GENERATE_FAILED", ex.getTargetException());
        }
    }
}
