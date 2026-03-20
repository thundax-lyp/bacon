package com.github.thundax.bacon.auth.interfaces.dto;

public record PasswordChangeRequest(String oldPassword, String newPassword) {
}
