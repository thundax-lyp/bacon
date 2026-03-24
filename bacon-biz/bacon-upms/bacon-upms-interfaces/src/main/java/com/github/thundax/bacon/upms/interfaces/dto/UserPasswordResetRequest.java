package com.github.thundax.bacon.upms.interfaces.dto;

public record UserPasswordResetRequest(Long tenantId, String newPassword) {
}
