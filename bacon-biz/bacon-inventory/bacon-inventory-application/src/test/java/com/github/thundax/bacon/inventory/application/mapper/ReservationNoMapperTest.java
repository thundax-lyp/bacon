package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationNoMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(ReservationNoMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(ReservationNoMapper.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToReservationNo() {
        assertThat(ReservationNoMapper.toDomain("RSV-001"))
                .isEqualTo(ReservationNo.of("RSV-001"));
    }
}
