package com.github.thundax.bacon.upms.interfaces.dto;

public record UserPasswordResetRequest(String tenantId, String newPassword) {
}
