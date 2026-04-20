package com.github.thundax.bacon.upms.api.response;

public record UserIdentityFacadeResponse(Long id, Long userId, String identityType, String identityValue, String status) {}
