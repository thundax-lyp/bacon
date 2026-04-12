package com.github.thundax.bacon.upms.infra.persistence.assembler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

final class UpmsPersistenceAssemblerSupport {

    private UpmsPersistenceAssemblerSupport() {}

    static LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
