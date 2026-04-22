package com.github.thundax.bacon.upms.interfaces.response;

import java.util.Set;

/**
 * 权限码集合响应对象。
 */
public record PermissionCodeSetResponse(Set<String> permissionCodes) {}
