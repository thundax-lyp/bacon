package com.github.thundax.bacon.common.core.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuditDataTest {

    @Test
    void shouldExposeAuditFields() {
        Instant createdAt = Instant.parse("2026-04-10T08:00:00Z");
        Instant updatedAt = Instant.parse("2026-04-10T09:00:00Z");

        AuditData auditData = new AuditData("creator", createdAt, "updater", updatedAt);

        assertThat(auditData.createdBy()).isEqualTo("creator");
        assertThat(auditData.createdAt()).isEqualTo(createdAt);
        assertThat(auditData.updatedBy()).isEqualTo("updater");
        assertThat(auditData.updatedAt()).isEqualTo(updatedAt);
    }
}
