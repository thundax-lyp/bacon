package com.github.thundax.bacon.common.id.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimestampedBizCodeFormatter {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private TimestampedBizCodeFormatter() {}

    public static String format(String domain, long id) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String suffix = String.format("%06d", Math.floorMod(id, 1_000_000L));
        return domain + timestamp + "-" + suffix;
    }
}
