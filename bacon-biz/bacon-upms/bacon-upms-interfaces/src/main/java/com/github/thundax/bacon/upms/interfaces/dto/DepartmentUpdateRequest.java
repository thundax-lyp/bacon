package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentUpdateRequest(String code, String name, Long parentId, String leaderUserId, Integer sort) {}
