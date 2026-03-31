package com.github.thundax.bacon.upms.infra.repository.impl;

final class UpmsRepositoryHelper {

    private UpmsRepositoryHelper() {
    }

    static String userKey(Long tenantId, Long userId) {
        return tenantId + ":" + userId;
    }

    static String identityKey(Long tenantId, String identityType, String identityValue) {
        return tenantId + ":" + identityType + ":" + identityValue;
    }

    static String departmentKey(Long tenantId, Long departmentId) {
        return tenantId + ":" + departmentId;
    }

    static String departmentCodeKey(Long tenantId, String code) {
        return tenantId + ":" + code;
    }

    static String roleKey(Long tenantId, Long roleId) {
        return tenantId + ":" + roleId;
    }

    static String menuKey(Long tenantId, Long menuId) {
        return tenantId + ":" + menuId;
    }

    static String postKey(Long tenantId, Long postId) {
        return tenantId + ":" + postId;
    }

    static String resourceKey(Long tenantId, Long resourceId) {
        return tenantId + ":" + resourceId;
    }
}
