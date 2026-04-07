package com.github.thundax.bacon.inventory.domain.model.valueobject;

/**
 * 库存预占业务单号。
 */
public record ReservationNo(String value) {

    public ReservationNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("reservationNo must not be blank");
        }
    }

    public static ReservationNo of(String value) {
        return new ReservationNo(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
