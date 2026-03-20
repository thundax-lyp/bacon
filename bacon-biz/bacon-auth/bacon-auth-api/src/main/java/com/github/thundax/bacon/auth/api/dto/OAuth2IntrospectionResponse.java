package com.github.thundax.bacon.auth.api.dto;

public record OAuth2IntrospectionResponse(
        boolean active,
        String client_id,
        String scope,
        String sub,
        String tenant_id,
        long exp) {
}
