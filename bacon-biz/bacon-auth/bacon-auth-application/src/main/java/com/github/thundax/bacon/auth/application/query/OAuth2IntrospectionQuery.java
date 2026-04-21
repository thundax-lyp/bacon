package com.github.thundax.bacon.auth.application.query;

public record OAuth2IntrospectionQuery(String token, String clientId, String clientSecret) {}
