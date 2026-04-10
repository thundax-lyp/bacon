package com.github.thundax.bacon.common.core.valueobject;

import java.time.Instant;

/**
 * 只读审计展示信息。
 */
public record AuditData(String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {}
