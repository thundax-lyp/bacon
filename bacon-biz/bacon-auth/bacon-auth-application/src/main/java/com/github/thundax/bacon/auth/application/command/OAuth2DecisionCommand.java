package com.github.thundax.bacon.auth.application.command;

public record OAuth2DecisionCommand(String authorizationRequestId, String decision) {}
