package com.github.thundax.bacon.upms.domain.model.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantNoTest {

    @Test
    void shouldTrimTenantNoValue() {
        TenantNo tenantNo = new TenantNo(" tenant-demo ");

        assertThat(tenantNo.value()).isEqualTo("tenant-demo");
    }

    @Test
    void shouldRejectBlankTenantNo() {
        assertThatThrownBy(() -> new TenantNo("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tenantNo cannot be blank");
    }
}
