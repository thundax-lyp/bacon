package com.github.thundax.bacon.upms.interfaces.dto;

public record UserPageQueryRequest(Long tenantId, String account, String name, String phone, String status,
                                   Integer pageNo, Integer pageSize) {
}
