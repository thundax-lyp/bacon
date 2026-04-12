package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentCreateRequest(String code, String name, String parentId, String leaderUserId, Integer sort) {}
