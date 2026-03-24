package com.github.thundax.bacon.upms.interfaces.dto;

public record ResourceUpdateRequest(Long tenantId, String code, String name, String resourceType,
                                    String httpMethod, String uri, String status) {
}
