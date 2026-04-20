package com.github.thundax.bacon.upms.api.response;

public record UserFacadeResponse(
        Long id,
        String account,
        String name,
        String avatarStoredObjectNo,
        String phone,
        String departmentCode,
        String avatarUrl,
        String status) {}
