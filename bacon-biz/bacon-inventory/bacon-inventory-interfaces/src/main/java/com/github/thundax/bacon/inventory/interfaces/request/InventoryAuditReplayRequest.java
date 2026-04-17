package com.github.thundax.bacon.inventory.interfaces.request;

public record InventoryAuditReplayRequest(Long operatorId, String replayKey) {}
