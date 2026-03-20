package com.github.thundax.bacon.auth.interfaces.dto;

public record OAuth2DecisionRequest(String authorizationRequestId, String decision) {
}
