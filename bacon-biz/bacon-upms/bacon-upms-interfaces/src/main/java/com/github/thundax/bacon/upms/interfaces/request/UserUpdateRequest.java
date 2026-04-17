package com.github.thundax.bacon.upms.interfaces.request;

public record UserUpdateRequest(String account, String name, String phone, Long departmentId) {}
