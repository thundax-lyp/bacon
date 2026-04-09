package com.github.thundax.bacon.inventory.domain.repository;

public interface InventoryLogRepository
        extends InventoryAuditRecordRepository,
                InventoryAuditOutboxRepository,
                InventoryAuditDeadLetterRepository,
                InventoryAuditReplayTaskRepository {}
