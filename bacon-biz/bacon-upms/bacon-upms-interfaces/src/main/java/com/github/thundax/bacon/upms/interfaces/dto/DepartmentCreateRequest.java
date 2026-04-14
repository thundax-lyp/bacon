package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentCreateRequest(String code, String name, Long parentId, Long leaderUserId, Integer sort) {}
