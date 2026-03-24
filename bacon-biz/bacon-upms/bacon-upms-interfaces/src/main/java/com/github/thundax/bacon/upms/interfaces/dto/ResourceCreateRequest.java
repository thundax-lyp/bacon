package com.github.thundax.bacon.upms.interfaces.dto;

public record ResourceCreateRequest(Long tenantId, String code, String name, String resourceType,
                                    String httpMethod, String uri) {
}
