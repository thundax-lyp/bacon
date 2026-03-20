package com.github.thundax.bacon.auth.interfaces.dto;

public record SmsLoginRequest(String phone, String smsCaptcha) {
}
