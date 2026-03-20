package com.github.thundax.bacon.auth.api.dto;

public record OAuth2UserinfoResponse(String sub, String tenant_id, String name) {
}
