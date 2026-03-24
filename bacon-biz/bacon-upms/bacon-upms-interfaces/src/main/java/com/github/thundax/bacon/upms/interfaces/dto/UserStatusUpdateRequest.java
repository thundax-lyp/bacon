package com.github.thundax.bacon.upms.interfaces.dto;

public record UserStatusUpdateRequest(Long tenantId, String status) {
}
