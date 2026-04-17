package com.github.thundax.bacon.upms.interfaces.request;

public record UserCreateRequest(String account, String name, String phone, Long departmentId) {}
