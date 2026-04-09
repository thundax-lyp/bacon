package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationNoCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(ReservationNoCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(ReservationNoCodec.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToReservationNo() {
        assertThat(ReservationNoCodec.toDomain("RSV-001"))
                .isEqualTo(ReservationNo.of("RSV-001"));
    }
}
